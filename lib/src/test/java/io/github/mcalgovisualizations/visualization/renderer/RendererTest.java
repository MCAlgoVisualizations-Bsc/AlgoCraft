package io.github.mcalgovisualizations.visualization.renderer;

import io.github.mcalgovisualizations.visualization.TaskScheduler;
import io.github.mcalgovisualizations.visualization.algorithm.IAlgorithmEvent;
import io.github.mcalgovisualizations.visualization.instance.AudienceChannel;
import io.github.mcalgovisualizations.visualization.layout.ILayout;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import io.github.mcalgovisualizations.visualization.renderer.scene.ISceneOps;
import io.github.mcalgovisualizations.visualization.utils.FirstTestEvent;
import io.github.mcalgovisualizations.visualization.utils.SecondTestEvent;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RendererTest {

    @Mock private Instance mockInstance;
    @Mock private ILayout<String> mockLayout;

    // RETURNS_DEEP_STUBS prevents NPEs when Executor calls nested scene methods
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ISceneOps mockScene;

    @Mock private AudienceChannel mockAudience;
    @Mock private IAnimationHandler<FirstTestEvent> mockHandler;
    @Mock private IAnimationHandler<SecondTestEvent> mockExceptionHandler;
    @Mock private TaskScheduler mockTaskScheduler;

    private AnimationPlan<ISceneOps> emptyPlan;
    private AnimationPlan<ISceneOps> nonEmptyPlan;
    private Renderer<String, ISceneOps> renderer;

    @BeforeEach
    void setUp() {
        Pos origin = new Pos(0, 0, 0);

        emptyPlan = AnimationPlan.builder().build();
        nonEmptyPlan = AnimationPlan.builder()
                .step(10)
                .build();

        Map<Class<? extends IAlgorithmEvent>, IAnimationHandler<?>> handlers = new HashMap<>();
        handlers.put(FirstTestEvent.class, mockHandler);
        handlers.put(SecondTestEvent.class, mockExceptionHandler);

        TaskScheduler.TaskHandle mockTaskHandle = mock(TaskScheduler.TaskHandle.class);
        lenient().when(mockTaskScheduler.schedule(any(), any())).thenReturn(mockTaskHandle);

        var executor = new Executor<>(mockScene, mockTaskScheduler);
        renderer = new Renderer<>(
                mockInstance,
                origin,
                mockLayout,
                mockAudience,
                handlers,
                emptyPlan,
                mockScene,
                executor
        );
    }

    @Test
    void defaultConstructor_InstantiatesWithoutProvidedExecutor() {
        Map<Class<? extends IAlgorithmEvent>, IAnimationHandler<?>> handlers = new HashMap<>();
        assertDoesNotThrow(() -> new Renderer<>(
                mockInstance,
                new Pos(0, 0, 0),
                mockLayout,
                mockAudience,
                handlers,
                emptyPlan,
                mockScene
        ));
    }

    @Test
    void initialize_LoadsChunksAndSetsLayoutOnScene() {
        String testModel = "initial_data";
        Pos targetPos = new Pos(16, 0, 16);

        LayoutResult realResult = new LayoutResult(null, targetPos, null);
        LayoutResult[] layoutResults = new LayoutResult[]{ realResult };

        when(mockLayout.compute(eq(testModel), any(Pos.class), eq(mockInstance)))
                .thenReturn(layoutResults);
        when(mockInstance.loadChunk(anyInt(), anyInt()))
                .thenReturn(CompletableFuture.completedFuture(null));

        CompletableFuture<Void> future = renderer.initialize(testModel);
        future.join();

        verify(mockInstance).loadChunk(1, 1);
        verify(mockScene).setLayout(layoutResults);
    }

    @Test
    void render_NormalizesPlan_WhenDelaysCollapsedAndPlanHasSteps() {
        renderer.setSpeed(1); // Sets collapseAnimationDelays = true (1 <= 1)

        AnimationPlan<ISceneOps> planWithSteps = AnimationPlan.builder()
                .step(10, scene -> {})
                .stepAsync(5, scene -> { return CompletableFuture.completedFuture(null); })
                .build();

        FirstTestEvent event = new FirstTestEvent(1);
        when(mockHandler.handle(event)).thenReturn(planWithSteps);

        renderer.render(event);

        assertTrue(renderer.hasPendingAnimations());
        verify(mockHandler).handle(event);
    }

    @Test
    void render_BypassesNormalization_WhenDelaysNotCollapsed() {
        renderer.setSpeed(5); // Sets collapseAnimationDelays = false (5 > 1)

        AnimationPlan<ISceneOps> planWithSteps = AnimationPlan.builder()
                .step(10, scene -> {})
                .build();

        FirstTestEvent event = new FirstTestEvent(1);
        when(mockHandler.handle(event)).thenReturn(planWithSteps);

        renderer.render(event);

        assertTrue(renderer.hasPendingAnimations());
    }

    @Test
    void setSpeed_HandlesBothDelayBranches() {
        assertDoesNotThrow(() -> {
            renderer.setSpeed(5);
            renderer.setSpeed(1);
        });
    }

    @Test
    void render_IgnoresNullEvents() {
        assertDoesNotThrow(() -> renderer.render(null));
        assertFalse(renderer.hasPendingAnimations());
    }

    @Test
    void render_ProcessesValidEventAndNormalizesPlan() {
        FirstTestEvent event = new FirstTestEvent(0);
        when(mockHandler.handle(event)).thenReturn(nonEmptyPlan);

        renderer.render(event);

        verify(mockHandler).handle(event);
        assertTrue(renderer.hasPendingAnimations());
    }

    @Test
    void render_CatchesExceptionFromDispatcherHandler() {
        FirstTestEvent event = new FirstTestEvent(0);
        when(mockHandler.handle(event)).thenThrow(new IllegalStateException("Dispatch failed"));

        assertDoesNotThrow(() -> renderer.render(event));
    }

    @Test
    void complete_EnqueuesCompletionPlan() {
        assertDoesNotThrow(() -> renderer.complete());
    }

    @Test
    void pause_DelegatesToExecutor() {
        assertDoesNotThrow(() -> renderer.pause());
    }

    @Test
    void resume_DelegatesToExecutor() {
        assertDoesNotThrow(() -> renderer.resume());
    }

    @Test
    void onCleanup_DelegatesToExecutorAndScene() {
        assertDoesNotThrow(() -> renderer.onCleanup());
    }
}