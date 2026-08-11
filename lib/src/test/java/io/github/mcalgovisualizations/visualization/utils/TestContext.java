package io.github.mcalgovisualizations.visualization.utils;

import io.github.mcalgovisualizations.visualization.algorithm.IAlgorithmEvent;
import io.github.mcalgovisualizations.visualization.models.AlgorithmContext;

import java.util.ArrayList;
import java.util.List;

public class TestContext implements AlgorithmContext<List<Integer>> {
        private List<Integer> data;
        private final List<IAlgorithmEvent> events = new ArrayList<>();

        public TestContext(List<Integer> data) {
            this.data = new ArrayList<>(data);
        }

        @Override
        public List<Integer> copyData() {
            return new ArrayList<>(data);
        }

        @Override
        @SuppressWarnings("unchecked")
        public TestContext copy() {
            var copy = new TestContext(data);
            copy.events.addAll(events);
            return copy;
        }

        @Override
        public List<Integer> randomizeData() {
            this.data = List.of(99);
            return copyData();
        }

    @Override
    public List<Integer> getData() {
        return List.copyOf(data);
    }

    @Override
        public List<IAlgorithmEvent> getEvents() {
            return events;
        }

    @Override
    public void emit(IAlgorithmEvent... e) {

    }

    public void setData(List<Integer> data) {
        this.data = data;
    }

    public void addEvent(IAlgorithmEvent event) {
        this.events.add(event);
    }
}
