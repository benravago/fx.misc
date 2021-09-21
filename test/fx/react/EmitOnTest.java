package fx.react;

import org.junit.jupiter.api.Test;

class EmitOnTest {

  @Test
  void testStackOverflow() {
    var stream = new EventSource<Void>();
    var impulse = new EventSource<Void>();

    stream.emitOn(impulse).subscribe(x -> impulse.push(null));

    stream.push(null);

    // we are testing that there is no stack overflow at this line
    impulse.push(null);
  }

}
