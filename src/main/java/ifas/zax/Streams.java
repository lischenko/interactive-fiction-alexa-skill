package ifas.zax;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;

public class Streams {
    private static ByteBuffer stringToBuf(String c) {
        ByteBuffer buf = ByteBuffer.allocate(c.length()*2);
        buf.clear();
        buf.put(c.getBytes());
        return buf;
    }

    public static void writeStringToPipe(String s, Pipe pipe) throws IOException {
        ByteBuffer buf = stringToBuf(s);
        buf.flip();
        Pipe.SinkChannel sinkChannel = pipe.sink();
        while(buf.hasRemaining()) {
            sinkChannel.write(buf);
        }
    }

    public static String readStringFromPipe(Pipe pipe, int capacity) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(capacity);
        int bytesRead = pipe.source().read(buf);
        byte[] bytes = buf.array();
        String s = new String(bytes, 0, bytesRead);
        buf.flip();
        return s;
    }
}
