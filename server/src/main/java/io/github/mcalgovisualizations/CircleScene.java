package io.github.mcalgovisualizations;

import io.github.mcalgovisualizations.Displays.EntityCreatureDisplay;
import io.github.mcalgovisualizations.visualization.renderer.IDisplayValue;
import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import io.github.mcalgovisualizations.visualization.renderer.scene.AbstractScene;
import io.github.mcalgovisualizations.visualization.renderer.scene.SceneContext;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CircleScene extends AbstractScene {

    private final Map<Integer, Pos> homePositions = new HashMap<>();

    private EntityCreatureDisplay iTracker;
    private EntityCreatureDisplay jTracker;

    private Integer trackedISlot;
    private Integer trackedJSlot;

    private EntityCreatureDisplay trackedIDisplay;
    private EntityCreatureDisplay trackedJDisplay;

    private StagedCompare stagedCompare;

    public CircleScene(@NotNull SceneContext context) {
        super(context);
    }

    @Override
    public void setLayout(LayoutResult[] layoutResults) {
        clearCompareState();
        clearTrackers();

        displaysBySlot.clear();
        homePositions.clear();

        for (int i = 0; i < layoutResults.length; i++) {
            var result = layoutResults[i];
            if (result == null) continue;

            var display = result.displayValue();
            var pos = result.pos();

            displaysBySlot.put(i, display);
            homePositions.put(i, pos);

            display.setInstance(instance);
            display.teleport(pos);
            lookAtOrigin(display);
        }
    }

    public void trackI(int slot) {
        trackedISlot = slot;
        trackedIDisplay = null;
        refreshITracker();
    }

    public void trackJ(int slot) {
        trackedJSlot = slot;
        trackedJDisplay = null;
        refreshJTracker();
    }

    /**
     * Clears the tracker for the currently tracked display for the I loop.
     */
    public void clearITracker() {
        trackedISlot = null;
        trackedIDisplay = null;

        if (iTracker != null) {
            iTracker.remove();
            iTracker = null;
        }
    }

    /**
     * Clears the tracker for the currently tracked display for the J loop.
     */
    public void clearJTracker() {
        trackedJSlot = null;
        trackedJDisplay = null;

        if (jTracker != null) {
            jTracker.kill();
            jTracker = null;
        }
    }

    /**
     * Clears all trackers for the I and J loops.
     */
    public void clearTrackers() {
        clearITracker();
        clearJTracker();
    }

    public void shakeHead(int slot) {
        var dv = requireDisplay(slot);
        dv.shakeHead();
    }

    /**
     * Stages a compare pair for the given slots.
     * @param leftSlot 1st display slot
     * @param rightSlot 2nd display slot
     */
    public void stageCompare(int leftSlot, int rightSlot) {
        if (leftSlot == rightSlot) {
            throw new IllegalArgumentException("Cannot compare the same slot twice: " + leftSlot);
        }
        if (hasStagedCompare()) {
            throw new IllegalStateException("A compare pair is already staged.");
        }

        var leftDisplay = requireDisplay(leftSlot);
        var rightDisplay = requireDisplay(rightSlot);

        var leftHome = requireHomePosition(leftSlot);
        var rightHome = requireHomePosition(rightSlot);

        var leftComparePos = getLeftComparePosition();
        var rightComparePos = getRightComparePosition();
        var middle = getCompareMiddle();

        leftDisplay.teleport(leftComparePos);
        rightDisplay.teleport(rightComparePos);

        lookAtOrigin(leftDisplay);
        lookAtOrigin(rightDisplay);
        leftDisplay.lookAt(rightDisplay.getPos().add(0, 1, 0));
        rightDisplay.lookAt(leftDisplay.getPos());


        stagedCompare = new StagedCompare(
                leftSlot,
                rightSlot,
                leftDisplay,
                rightDisplay,
                leftHome,
                rightHome,
                leftComparePos,
                rightComparePos
        );

        emitLines(leftHome, middle);
        emitLines(rightHome, middle);
        refreshTrackers();
    }

    public void restoreStagedCompare() {
        if (!hasStagedCompare()) {
            return;
        }

        stagedCompare.leftDisplay.teleport(stagedCompare.leftHome);
        stagedCompare.rightDisplay.teleport(stagedCompare.rightHome);

        lookAtOrigin(stagedCompare.leftDisplay);
        lookAtOrigin(stagedCompare.rightDisplay);

        clearCompareState();
        refreshTrackers();
    }

    public void swapStagedComparePositions() {
        if (!hasStagedCompare()) {
            throw new IllegalStateException("No staged compare to swap in middle.");
        }

        stagedCompare.leftDisplay.teleport(stagedCompare.rightComparePos);
        stagedCompare.rightDisplay.teleport(stagedCompare.leftComparePos);

        lookAtOrigin(stagedCompare.leftDisplay);
        lookAtOrigin(stagedCompare.rightDisplay);

        refreshTrackers();
    }

    public void commitStagedSwap() {
        if (!hasStagedCompare()) {
            throw new IllegalStateException("No staged compare to commit.");
        }

        int leftSlot = stagedCompare.leftSlot;
        int rightSlot = stagedCompare.rightSlot;

        var leftDisplay = stagedCompare.leftDisplay;
        var rightDisplay = stagedCompare.rightDisplay;

        var leftHome = stagedCompare.leftHome;
        var rightHome = stagedCompare.rightHome;

        leftDisplay.teleport(rightHome);
        rightDisplay.teleport(leftHome);

        lookAtOrigin(leftDisplay);
        lookAtOrigin(rightDisplay);

        displaysBySlot.put(leftSlot, rightDisplay);
        displaysBySlot.put(rightSlot, leftDisplay);

        clearCompareState();
        refreshTrackers();
    }

    /**
     * Finishes the inner loop visuals by restoring the original positions of the displays.
     */
    public void finishInnerLoopVisuals() {
        if (hasStagedCompare()) {
            restoreStagedCompare();
        }
    }

    /**
     * Swaps the positions of two displays without moving them to the home position.
     * @param leftSlot 1st display slot
     * @param rightSlot 2nd display slot
     */
    public void swapDirect(int leftSlot, int rightSlot) {
        if (leftSlot == rightSlot) {
            return;
        }

        var leftDisplay = requireDisplay(leftSlot);
        var rightDisplay = requireDisplay(rightSlot);

        var leftHome = requireHomePosition(leftSlot);
        var rightHome = requireHomePosition(rightSlot);

        leftDisplay.teleport(rightHome);
        rightDisplay.teleport(leftHome);

        lookAtOrigin(leftDisplay);
        lookAtOrigin(rightDisplay);

        displaysBySlot.put(leftSlot, rightDisplay);
        displaysBySlot.put(rightSlot, leftDisplay);

        clearCompareState();
        refreshTrackers();
    }

    /**
     * Moves all displays to their home positions.
     */
    public void resetAllDisplaysToHome() {
        clearCompareState();

        for (var entry : homePositions.entrySet()) {
            int slot = entry.getKey();
            Pos home = entry.getValue();

            var display = requireDisplay(slot);
            display.teleport(home);
            lookAtOrigin(display);
        }

        refreshTrackers();
    }

    public boolean hasStagedCompare() {
        return stagedCompare != null;
    }

    public boolean isStagedPair(int a, int b) {
        if (!hasStagedCompare()) {
            return false;
        }

        return (stagedCompare.leftSlot == a && stagedCompare.rightSlot == b)
                || (stagedCompare.leftSlot == b && stagedCompare.rightSlot == a);
    }

    public EntityCreatureDisplay getStagedRightDisplay() {
        if (!hasStagedCompare()) {
            throw new IllegalStateException("No staged compare.");
        }
        return stagedCompare.rightDisplay;
    }

    public void clearCompareState() {
        stagedCompare = null;
    }

    public Pos getHomePosition(int slot) {
        return requireHomePosition(slot);
    }

    private void refreshTrackers() {
        refreshITracker();
        refreshJTracker();
    }

    private void refreshITracker() {
        Pos base = resolveITrackerBase();
        if (base == null) {
            return;
        }

        Pos trackerPos = getITrackerPosition(base);


        if (iTracker == null) {
            iTracker = new EntityCreatureDisplay(trackerPos, EntityType.CHICKEN, "I");
            iTracker.setInstance(instance);
        }

        iTracker.teleport(trackerPos);
        iTracker.lookAt(base.add(0, requireDisplay(trackedISlot).getEyeHeight() + 2, 0));
    }

    private void refreshJTracker() {
        Pos base = resolveJTrackerBase();
        if (base == null) {
            return;
        }

        Pos trackerPos = getJTrackerPosition(base);

        if (jTracker == null) {
            jTracker = new EntityCreatureDisplay(trackerPos, EntityType.CHICKEN, "J");
            jTracker.setInstance(instance);
        }

        jTracker.teleport(trackerPos);
        jTracker.lookAt(base.add(0, requireDisplay(trackedJSlot).getEyeHeight() + 2, 0));
    }

    private EntityCreatureDisplay resolveTrackedDisplay(EntityCreatureDisplay explicitDisplay, Integer slot) {
        if (explicitDisplay != null) {
            return explicitDisplay;
        }

        if (slot != null) {
            return requireDisplay(slot);
        }

        return null;
    }

    private Pos resolveITrackerBase() {
        if (trackedIDisplay != null) {
            return trackedIDisplay.getPos();
        }

        if (trackedISlot != null) {
            return requireHomePosition(trackedISlot);
        }

        return null;
    }

    private Pos resolveJTrackerBase() {
        if (trackedJDisplay != null) {
            return trackedJDisplay.getPos();
        }

        if (trackedJSlot != null) {
            return requireDisplay(trackedJSlot).getPos();
        }

        return null;
    }

    public EntityCreatureDisplay requireDisplay(int slot) {
        return (EntityCreatureDisplay) super.requireDisplay(slot);
    }

    private Pos requireHomePosition(int slot) {
        var pos = homePositions.get(slot);
        if (pos == null) {
            throw new IllegalArgumentException("No home position for slot " + slot);
        }
        return pos;
    }

    private Pos getLeftComparePosition() {
        return origin.add(-2.0, 5.0, 0.0);
    }

    private Pos getRightComparePosition() {
        return origin.add(2.0, 5.0, 0.0);
    }

    private Pos getCompareMiddle() {
        return origin.add(0.0, 5.0, 0.0);
    }

    private Pos getITrackerPosition(Pos base) {
        var tracked = requireDisplay(trackedISlot);
        return base.add(-0.8, tracked.getEyeHeight() + 2, 0.0);
    }

    private Pos getJTrackerPosition(Pos base) {
        var tracked = requireDisplay(trackedJSlot);
        return base.add(0.8, tracked.getEyeHeight() + 2, 0.0);
    }

    private void lookAtOrigin(IDisplayValue display) {
        if (display instanceof EntityCreatureDisplay creature) {
            creature.lookAt(origin);
        }
    }

    @Override
    public void cleanUp() {
        clearCompareState();
        clearTrackers();
        super.cleanUp();
        homePositions.clear();
    }

    public void emitLines(Pos from, Pos to) {
        if (from == null || to == null || instance == null) {
            return;
        }

        double dx = to.x() - from.x();
        double dy = to.y() - from.y();
        double dz = to.z() - from.z();
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        int steps = Math.max(2, (int) Math.ceil(distance * 6.0));

        for (int i = 1; i < steps; i++) {
            double t = (double) i / steps;

            Pos point = new Pos(
                    from.x() + (dx * t),
                    from.y() + (dy * t),
                    from.z() + (dz * t),
                    0f,
                    0f
            );

            ParticlePacket packet = new ParticlePacket(
                    Particle.CRIT,
                    true,
                    true,
                    point,
                    Vec.ZERO,
                    0f,
                    1
            );

            for (var viewer : instance.getPlayers()) {
                viewer.sendPacket(packet);
            }
        }
    }

    private static final class StagedCompare {
        private final int leftSlot;
        private final int rightSlot;

        private final EntityCreatureDisplay leftDisplay;
        private final EntityCreatureDisplay rightDisplay;

        private final Pos leftHome;
        private final Pos rightHome;

        private final Pos leftComparePos;
        private final Pos rightComparePos;

        private StagedCompare(
                int leftSlot,
                int rightSlot,
                IDisplayValue leftDisplay,
                IDisplayValue rightDisplay,
                Pos leftHome,
                Pos rightHome,
                Pos leftComparePos,
                Pos rightComparePos
        ) {
            this.leftSlot = leftSlot;
            this.rightSlot = rightSlot;
            this.leftDisplay = (EntityCreatureDisplay) Objects.requireNonNull(leftDisplay);
            this.rightDisplay = (EntityCreatureDisplay) Objects.requireNonNull(rightDisplay);
            this.leftHome = Objects.requireNonNull(leftHome);
            this.rightHome = Objects.requireNonNull(rightHome);
            this.leftComparePos = Objects.requireNonNull(leftComparePos);
            this.rightComparePos = Objects.requireNonNull(rightComparePos);
        }
    }
}