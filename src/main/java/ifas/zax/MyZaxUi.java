package ifas.zax;

import com.zaxsoft.zmachine.ZUserInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.nio.channels.Pipe;
import java.util.Vector;

/**
 * ZAX has its own idea about what IF UI should look like, and oftentimes it is not really suitable for voice UI. 
 * There are sevearl hacks to adapt the GUI-based API to voice interface.
 */
public class MyZaxUi implements ZUserInterface {
    private static final Logger LOG = LoggerFactory.getLogger(MyZaxUi.class);

    private final Pipe storyTextStream = Pipe.open();

    private final Pipe userInputStream = Pipe.open();

    private String nextFilename = "/tmp/ifas-save.file"; //XXX

    public MyZaxUi() throws IOException {}

    public Pipe getUserInputStream() { return userInputStream; }

    public Pipe getStoryTextStream() { return storyTextStream; }

    /** This was supposed to prompt user for a filename. We replaced it with {@link #nextFilename} 
     * state variable which is set programmatically using {@link #setNextFilename(String)}.
     */
    @Override public String getFilename(String title, String suggested, boolean saveFlag) {
        return nextFilename;
    }

    @Override public Dimension getScreenCharacters() {
        return new Dimension(8192, 8192);
    }

    @Override public Dimension getScreenUnits() {
        return new Dimension(8192, 8192);
    }

    @Override public Dimension getFontSize() {
        return new Dimension(1, 1);
    }

    @Override public Dimension getWindowSize(int window) {
        return new Dimension(8192, 8192);
    }

    @Override public Point getCursorPosition() {
        return new Point(0, 0);
    }

    /**
     * Implementation of getting a command text from the user.
     */
    @Override public int readLine(StringBuffer sb, int time) {
        try {
            String s = Streams.readStringFromPipe(userInputStream, 1024);
            sb.append(s);
            return s.getBytes().length;
        } catch (IOException e) {
            LOG.error("Error while copying story text", e);
            return 0;
        }
    }

    @Override public int readChar(int time) {
        return 0;
    }

    /**
     * This method gets called by ZAX when story text updates.
     * This implementation forwards them to a pipe the skill can read.
     */
    @Override public void showString(String s) {
        try {
            Streams.writeStringToPipe(s, storyTextStream);
        } catch (IOException e) {
            MyZaxUi.LOG.error("Error while copying story text", e);
        }
    }

    @Override public int getDefaultForeground() {
        return 1;
    }

    @Override public int getDefaultBackground() {
        return 0;
    }

    @Override public boolean defaultFontProportional()  { return false; }
    @Override public boolean hasBoldface()              { return false; }
    @Override public boolean hasColors()                { return false; }
    @Override public boolean hasFixedWidth()            { return false; }
    @Override public boolean hasItalic()                { return false; }
    @Override public boolean hasStatusLine()            { return false; }
    @Override public boolean hasTimedInput()            { return false; }
    @Override public boolean hasUpperWindow()           { return false; }

    @Override public void eraseLine(int s)                                      {}
    @Override public void eraseWindow(int window)                               {}
    @Override public void fatal(String errmsg)                                  {}
    @Override public void initialize(int ver)                                   {}
    @Override public void quit()                                                {
        try {
            storyTextStream.source().close();
            storyTextStream.sink().close();
            userInputStream.source().close();
            storyTextStream.sink().close();
        } catch (IOException e) {
            LOG.error("Error while quitting", e);
        }

    }
    @Override public void restart()                                             {}
    @Override public void scrollWindow(int lines)                               {}
    @Override public void setColor(int fg, int bg)                              {}
    @Override public void setCurrentWindow(int window)                          {}
    @Override public void setCursorPosition(int x, int y)                       {}
    @Override public void setFont(int font)                                     {}
    @Override public void setTerminatingCharacters(Vector chars)                {}
    @Override public void setTextStyle(int style)                               {}
    @Override public void showStatusBar(String s, int a, int b, boolean flag)   {}
    @Override public void splitScreen(int lines)                                {}

    public synchronized void setNextFilename(String filename) {
        this.nextFilename = filename;
    }
}
