package com.github.cadecode.java.practice.mq.tibrv.example;/*
 * Copyright (c) 1998-$Date: 2013-12-20 07:48:17 -0800 (Fri, 20 Dec 2013) $ TIBCO Software Inc.
 * All rights reserved.
 * TIB/Rendezvous is protected under US Patent No. 5,187,787.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 *
 */

/*
 * tibrvsend - sample Rendezvous message publisher
 *
 * This program publishes one or more string messages on a specified
 * subject.  Both the subject and the message(s) must be supplied as
 * command parameters.  Message(s) with embedded spaces should be quoted.
 * A field named "DATA" will be created to hold the string in each
 * message.
 *
 * Optionally the user may specify communication parameters for
 * tibrvTransport_Create.  If none are specified, default values
 * are used.  For information on default values for these parameters,
 * please see the TIBCO/Rendezvous Concepts manual.
 *
 *
 * Normally a listener such as tibrvlisten should be started first.
 *
 * Examples:
 *
 *  Publish two messages on subject a.b.c and default parameters:
 *   java tibrvsend a.b.c "This is my first message" "This is my second message"
 *
 *  Publish a message on subject a.b.c using port 7566:
 *   java tibrvsend -service 7566 a.b.c message
 */

import com.tibco.tibrv.*;

public class tibrvsend {

    String service = null;
    String network = null;
    String daemon = null;

    String filedName = "DATA";
    String sendSubject = "COM.TEST";

    public tibrvsend(String[] args) {
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

        // Create the message
        TibrvMsg msg = new TibrvMsg();

        // Set send subject into the message
        try {
            msg.setSendSubject(sendSubject);
        } catch (TibrvException e) {
            System.err.println("Failed to set send subject:");
            e.printStackTrace();
            System.exit(0);
        }

        int index = 0;
        while (true) {
            try {

                System.out.println("Publishing: subject=" + msg.getSendSubject() + " \"" + index + "\"");
                msg.update(filedName, index++);
                transport.send(msg);
                Thread.sleep(2000L);
            } catch (Exception e) {
                System.err.println("Error sending a message:");
                e.printStackTrace();
                System.exit(0);
            }
        }

    }

    public static void main(String[] args) {
        new tibrvsend(args);
    }

}
