package fiji.plugin.trackmate;

import java.awt.Color;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * This class is used to log messages occurring during plugin execution. 
 */
public abstract class Logger extends PrintWriter {
	
	public Logger() {
		// Call super with a dummy writer
		super(new Writer() {
			@Override
			public void write(char[] cbuf, int off, int len) throws IOException {}			
			@Override
			public void flush() throws IOException {}			
			@Override
			public void close() throws IOException {}
		});
		// Replace by a useful writer
		this.out = new Writer() {
			@Override
			public void close() throws IOException {}
			@Override
			public void flush() throws IOException {}
			@Override
			public void write(char[] cbuf, int off, int len) throws IOException {
				String str = ""; 
				for (int i = off; i < len; i++)
					str += cbuf[i];
				log(str);
			}			
		};
	}

	public static final Color NORMAL_COLOR = Color.BLACK;
	public static final Color ERROR_COLOR = new Color(0.8f, 0, 0);
	public static final Color GREEN_COLOR = new Color(0, 0.6f, 0);
	public static final Color BLUE_COLOR = new Color(0, 0, 0.7f);
	
	/**
	 * Append the message to the logger, with the specified color.
	 */
	public abstract void log(String message, Color color);
	
	/**
	 * Send the message to the error channel of this logger.
	 */
	public abstract void error(String message);
	
	/**
	 * Append the message to the logger with default black color.
	 */
	public void log(String message)  { log(message, NORMAL_COLOR);	}
	
	/**
	 * Set the progress value of the process logged by this logger. 
	 * Values should be between 0 and 1, 1 meaning the process if finished.
	 */
	public abstract void setProgress(float val);
	
}