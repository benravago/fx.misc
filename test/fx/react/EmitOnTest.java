package fx.react;

import org.junit.jupiter.api.Test;

class EmitOnTest {

  @Test
  void testStackOverflow() {
    EventSource<Void> stream = new EventSource<>();
    EventSource<Void> impulse = new EventSource<>();

    stream.emitOn(impulse).subscribe(x -> impulse.push(null));

    stream.push(null);

    // we are testing that there is no stack overflow at this line
    impulse.push(null);
  }

}
