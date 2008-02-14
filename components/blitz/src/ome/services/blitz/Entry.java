/*   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz;

import java.util.concurrent.locks.ReentrantLock;

import ome.system.OmeroContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 * OMERO.blitz startup code. Replaces {@link Main} as the main application entry
 * point. Uses Sun-specific APIs to handle signals.
 */
public class Entry {

    private final static Log log = LogFactory.getLog(Entry.class);

    /**
     * Return code status
     */
    int status = 0;

    /**
     * Name of the {@link OmeroContext} to
     */
    final String name;

    /**
     * Prevents multiple calls to {@link #shutdown()}
     */
    final ReentrantLock lock = new ReentrantLock();

    /**
     * Context for the OMERO application
     */
    volatile OmeroContext ctx = null;

    /**
     * {@link Ice.Communicator} which will be waited on.
     */
    volatile Ice.Communicator ic = null;

    /**
     * Entry point to the server. The first argument on the command line will be
     * used as the name for the {@link OmeroContext} via
     * {@link Main2#Main(String)}. Other options include:
     * 
     * -s Check status (all args passed to {@link Ice.Util.initialize(String[])}
     * 
     */
    public static void main(final String[] args) {
        String name = "OMERO.blitz";
        if (args != null && args.length > 0) {
            if ("-s".equals(args[0])) {
                try {
                    new Status(args).run();
                } catch (Throwable t) {
                    System.exit(1);
                }
                System.exit(0);
            }
            // Now we find the first non-"--Ice.Config" argument and
            // pass that to Main(). The last --Ice.Config value will be
            // seen by the Ice.Communicator.
            for (String string : args) {
                if (string.startsWith("--Ice.Config")) {
                    System.setProperty("ICE_CONFIG", string);
                } else {
                    name = string;
                }
            }
        }

        final Entry instance = new Entry(name);

        SignalHandler handler = new SignalHandler() {
            public void handle(Signal sig) {
                log.info(sig.getName() + ": Shutdown requested.");
                instance.lock.lock();
                instance.shutdown();
                System.exit(sig.getNumber());
            }
        };

        Signal.handle(new Signal("INT"), handler);
        Signal.handle(new Signal("TERM"), handler);

        instance.start();
    }

    /**
     * Stores name of the {@link OmeroContext} which is to be used by this
     * instance.
     */
    public Entry(String name) {
        this.name = name;
    }

    /**
     * Obtains the {@link #name named} {@link OmeroContext}, creating it if
     * necessary, and then delegates to
     * {@link Ice.Communicator#waitForShutdown()} until either it is externally
     * shutdown, or until a signal is caught.
     */
    void start() {
        try {
            log.info("Creating " + name + ". Please wait...");
            ctx = OmeroContext.getInstance(name);
            ic = (Ice.Communicator) ctx.getBean("Ice.Communicator");
            log.info(name + " now accepting connections.");
            ic.waitForShutdown();
            status = 0;
        } catch (Ice.LocalException e) {
            log.error("Error on startup.", e);
            status = 1;
        } catch (Exception e) {
            log.error("Error on startup.", e);
            status = 2;
        }
        System.out.flush();
        System.err.flush();
        lock.lock();
        shutdown();
    }

    /**
     * Calls {@link OmeroContext#closeAll()} to recursively close all
     * OMERO.blitz resources in the reverse order that they were created.
     * 
     * Throws no exceptions.
     */
    void shutdown() {

        // Finally shutdown the whole context
        if (ctx != null) {
            try {
                log.info("Calling close on context " + name);
                ctx.closeAll();
                ctx = null;
                log.info("Finished shutdown.");
            } catch (Exception e) {
                log.error("Error shutting down " + name, e);
                status = 3;
            }
        }
        System.exit(status);
    }
}
