/*
 * Copyright (c) 2002-2007, Marc Prud'hommeaux. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package jline;

import java.io.*;

import org.fusesource.jansi.AnsiConsole;

/**
 *  Representation of the input terminal for a platform. Handles
 *  any initialization that the platform may need to perform
 *  in order to allow the {@link ConsoleReader} to correctly handle
 *  input.
 *
 *  @author  <a href="mailto:mwp1@cornell.edu">Marc Prud'hommeaux</a>
 */
public abstract class Terminal implements ConsoleOperations {
	
	static {
		// We need jansi hooked into the system.  This lets us have a consistent
		// ANSI output backend.
		AnsiConsole.systemInstall();
	}
	
	
    private static Terminal term;

    /**
     *  @see #setupTerminal
     */
    public static Terminal getTerminal() {
        return setupTerminal();
    }

    /** 
     *  Reset the current terminal to null. 
     */
    public static void resetTerminal() {
        term = null;
    }

    /**
     *  <p>Configure and return the {@link Terminal} instance for the
     *  current platform. This will initialize any system settings
     *  that are required for the console to be able to handle
     *  input correctly, such as setting tabtop, buffered input, and
     *  character echo.</p>
     *
     *  <p>This class will use the Terminal implementation specified in the
     *  <em>jline.terminal</em> system property, or, if it is unset, by
     *  detecting the operating system from the <em>os.name</em>
     *  system property and instantiating either the
     *  {@link WindowsTerminal} or {@link UnixTerminal}.
     *
     *  @see #initializeTerminal
     */
    public static synchronized Terminal setupTerminal() {
        if (term != null) {
            return term;
        }

        final Terminal t;

        String os = System.getProperty("os.name").toLowerCase();
        String termProp = System.getProperty("jline.terminal");

        if ((termProp != null) && (termProp.length() > 0)) {
            try {
                t = (Terminal) Class.forName(termProp).newInstance();
            } catch (Exception e) {
                throw (IllegalArgumentException) new IllegalArgumentException(e
                    .toString()).fillInStackTrace();
            }
        } else if (os.indexOf("windows") != -1) {
            t = new WindowsTerminal();
        } else {
            t = new UnixTerminal();
        }

        try {
            t.initializeTerminal();
        } catch (Exception e) {
            e.printStackTrace();

            return term = new UnsupportedTerminal();
        }

        return term = t;
    }

    /**
     *  @deprecated You can allways send ANSI to stdout now.
     *  			see {@link AnsiConsole} for more details. 
     */
    public boolean isANSISupported() {
        return true;
    }

    /**
     *  Read a single character from the input stream. This might
     *  enable a terminal implementation to better handle nuances of
     *  the console.
     */
    public int readCharacter(final InputStream in) throws IOException {
        return in.read();
    }

    /**
     *  Reads a virtual key from the console. Typically, this will
     *  just be the raw character that was entered, but in some cases,
     *  multiple input keys will need to be translated into a single
     *  virtual key.
     *
     *  @param  in  the InputStream to read from
     *  @return  the virtual key (e.g., {@link ConsoleOperations#VK_UP})
     */
    public int readVirtualKey(InputStream in) throws IOException {
        return readCharacter(in);
    }

    /**
     *  Initialize any system settings
     *  that are required for the console to be able to handle
     *  input correctly, such as setting tabtop, buffered input, and
     *  character echo.
     */
    public abstract void initializeTerminal() throws Exception;

    /**
     * Restore the original terminal configuration, which can be used when
     * shutting down the console reader. The ConsoleReader cannot be
     * used after calling this method.
     */
    public abstract void restoreTerminal() throws Exception;

    /**
     *  Returns the current width of the terminal (in characters)
     */
    public abstract int getTerminalWidth();

    /**
     *  Returns the current height of the terminal (in lines)
     */
    public abstract int getTerminalHeight();

    /**
     *  Returns true if this terminal is capable of initializing the
     *  terminal to use jline.
     */
    public abstract boolean isSupported();

    /**
     *  Returns true if the terminal will echo all characters type.
     */
    public abstract boolean getEcho();

    /**
     *  Invokes before the console reads a line with the prompt and mask.
     */
    public void beforeReadLine(ConsoleReader reader, String prompt,
                               Character mask) {
    }

    /**
     *  Invokes after the console reads a line with the prompt and mask.
     */
    public void afterReadLine(ConsoleReader reader, String prompt,
                              Character mask) {
    }

    /**
     *  Returns false if character echoing is disabled.
     */
    public abstract boolean isEchoEnabled();


    /**
     *  Enable character echoing. This can be used to re-enable character
     *  if the ConsoleReader is no longer being used.
     */
    public abstract void enableEcho();


    /**
     *  Disable character echoing. This can be used to manually re-enable
     *  character if the ConsoleReader has been disabled.
     */
    public abstract void disableEcho();

    public InputStream getDefaultBindings() {
        return Terminal.class.getResourceAsStream("keybindings.properties");
    }

    protected void resetTerminalIfThis() {
        if (term == this) {
            resetTerminal();
        }
    }
}
