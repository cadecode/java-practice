package com.github.cadecode.java.practice.mq.tibrv.example;/*
 * Copyright (c) 1998-$Date: 2013-12-20 07:48:17 -0800 (Fri, 20 Dec 2013) $ TIBCO Software Inc.
 * All rights reserved.
 * TIB/Rendezvous is protected under US Patent No. 5,187,787.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 *
 */

/*
 * tibrvlisten - generic Rendezvous subscriber
 *
 * This program listens for any number of messages on a specified
 * set of subject(s).  Message(s) received are printed.
 *
 * Some platforms require proper quoting of the arguments to prevent
 * the command line processor from modifying the command arguments.
 *
 * The user may terminate the program by typing Control-C.
 *
 * Optionally the user may specify communication parameters for
 * tibrvTransport_Create.  If none are specified, default values
 * are used.  For information on default values for these parameters,
 * please see the TIBCO/Rendezvous Concepts manual.
 *
 *
 * Examples:
 *
 * Listen to every message published on subject a.b.c:
 *  java tibrvlisten a.b.c
 *
 * Listen to every message published on subjects a.b.c and x.*.Z:
 *  java tibrvlisten a.b.c "x.*.Z"
 *
 * Listen to every system advisory message:
 *  java tibrvlisten "_RV.*.SYSTEM.>"
 *
 * Listen to messages published on subject a.b.c using port 7566:
 *  java tibrvlisten -service 7566 a.b.c
 *
 */

import com.tibco.tibrv.*;

import java.util.Date;

public class tibrvlisten implements TibrvMsgCallback {

    String service = null;
    String network = null;
    String daemon = null;

    String listenSubject = "TIBCO.TEST";

    public tibrvlisten(String[] args) {
        // open Tibrv in native implementation
        try {

            Tibrv.open(Tibrv.IMPL_NATIVE);
        } catch (TibrvException e) {
            System.err.println("Failed to open Tibrv in native implementation:");
            e.printStackTrace();
            System.exit(0);
        }

        // Create RVD transport
        TibrvTransport transport = null;
        try {
            transport = new TibrvRvdTransport(service, network, daemon);
        } catch (TibrvException e) {
            System.err.println("Failed to create TibrvRvdTransport:");
            e.printStackTrace();
            System.exit(0);
        }

        // create listener using default queue
        try {
            new TibrvListener(Tibrv.defaultQueue(), this, transport, listenSubject, null);
            System.err.println("Listening on: " + listenSubject);
        } catch (TibrvException e) {
            System.err.println("Failed to create listener:");
            e.printStackTrace();
            System.exit(0);
        }

        // dispatch Tibrv events
        while (true) {
            try {
                Tibrv.defaultQueue().dispatch();
            } catch (TibrvException e) {
                System.err.println("Exception dispatching default queue:");
                e.printStackTrace();
                System.exit(0);
            } catch (InterruptedException ie) {
                System.exit(0);
            }
        }
    }

    public void onMsg(TibrvListener listener, TibrvMsg msg) {
        System.out.println((new Date()) +
                ": subject=" + msg.getSendSubject() +
                ", reply=" + msg.getReplySubject() +
                ", message=" + msg
        );
        System.out.flush();
    }

    public static void main(String[] args) {
        new tibrvlisten(args);
    }

}
