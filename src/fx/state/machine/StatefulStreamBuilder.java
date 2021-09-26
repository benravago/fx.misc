package fx.state.machine;

import fx.react.EventStream;

public interface StatefulStreamBuilder<S, O> {

  <I> StatefulStreamBuilderOn<S, O, I> on(EventStream<I> input);

  /**
   * Returns an event stream that emits a value when one of the state
   * machine's input streams causes the state machine to emit a value.
   *
   * <p>The returned event stream is <em>lazily bound</em>, meaning the
   * associated state machine is subscribed to its inputs only when the
   * returned stream has at least one subscriber. No state transitions
   * take place unless there is a subscriber to the returned stream. If
   * you need to keep the state machine alive even when temporarily not
   * subscribed to the returned stream, you can <em>pin</em> the returned
   * stream.
   */
  EventStream<O> toEventStream();

}