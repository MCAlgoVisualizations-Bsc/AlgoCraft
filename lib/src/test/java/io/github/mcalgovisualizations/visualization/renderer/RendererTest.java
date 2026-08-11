package io.github.mcalgovisualizations.visualization.renderer;

import io.github.mcalgovisualizations.visualization.algorithm.IAlgorithmEvent;
import io.github.mcalgovisualizations.visualization.instance.AudienceChannel;
import io.github.mcalgovisualizations.visualization.layout.ILayout;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import io.github.mcalgovisualizations.visualization.renderer.scene.ISceneOps;
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
    @Mock private IAnimationHandler<?> mockHandler;
    @Mock private IAnimationHandler<?> mockExceptionHandler;

    private AnimationPlan<ISceneOps> emptyPlan;
    private AnimationPlan<ISceneOps> nonEmptyPlan;
    private Renderer<String, ISceneOps> renderer;

    private record TestEvent() implements IAlgorithmEvent {}
    private record ExceptionEvent() implements IAlgorithmEvent {}

    @BeforeEach
    void setUp() {
        Pos origin = new Pos(0, 0, 0);

        emptyPlan = AnimationPlan.<ISceneOps>builder().build();
        nonEmptyPlan = AnimationPlan.<ISceneOps>builder()
                .step(10)
                .build();

        Map<Class<? extends IAlgorithmEvent>, IAnimationHandler<?>> handlers = new HashMap<>();
        handlers.put(TestEvent.class, mockHandler);
        handlers.put(ExceptionEvent.class, mockExceptionHandler);

        renderer = new Renderer<>(
                mockInstance,
                origin,
                mockLayout,
                mockAudience,
                handlers,
                emptyPlan,
                mockScene
        );
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

//    @Test
//    void render_CatchesIllegalStateExceptionFromDispatcher() {
//        ExceptionEvent event = new ExceptionEvent();
//        doThrow(new IllegalStateException("Dispatch failed")).when(mockExceptionHandler).handle(any());
//
//        assertDoesNotThrow(() -> renderer.render(event));
//    }
//
//    @Test
//    void complete_EnqueuesCompletionPlan() {
//        assertDoesNotThrow(() -> renderer.complete());
//    }

}
