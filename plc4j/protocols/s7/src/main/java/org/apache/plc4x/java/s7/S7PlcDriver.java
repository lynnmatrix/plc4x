/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/
package org.apache.plc4x.java.s7;

import org.apache.plc4x.java.PlcDriver;
import org.apache.plc4x.java.authentication.PlcAuthentication;
import org.apache.plc4x.java.connection.PlcConnection;
import org.apache.plc4x.java.exceptions.PlcConnectionException;
import org.apache.plc4x.java.model.PlcReadRequest;
import org.apache.plc4x.java.model.PlcReadResponse;
import org.apache.plc4x.java.s7.connection.S7PlcConnection;
import org.apache.plc4x.java.s7.mina.model.params.ReadVarParameter;
import org.apache.plc4x.java.s7.mina.model.params.items.S7AnyReadVarRequestItem;
import org.apache.plc4x.java.s7.mina.model.types.MemoryArea;
import org.apache.plc4x.java.s7.mina.model.types.SpecificationType;
import org.apache.plc4x.java.s7.mina.model.types.TransportSize;
import org.apache.plc4x.java.model.Address;
import org.apache.plc4x.java.types.Datatype;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of the S7 protocol, based on:
 * - S7 Protocol
 * - ISO Transport Protocol (Class 0) (https://tools.ietf.org/html/rfc905)
 * - ISO on TCP (https://tools.ietf.org/html/rfc1006)
 * - TCP
 */
public class S7PlcDriver implements PlcDriver {

    private static final String S7_URI_PATTERN = "^s7://(.*?)/(\\d{1,4})/(\\d{1,4})";

    @Override
    public String getProtocolCode() {
        return "s7";
    }

    @Override
    public String getProtocolName() {
        return "Siemens S7 (Basic)";
    }

    @Override
    public PlcConnection connect(String url) throws PlcConnectionException {
        Pattern pattern = Pattern.compile(S7_URI_PATTERN);
        Matcher matcher = pattern.matcher(url);
        if (!matcher.matches()) {
            throw new PlcConnectionException(
                "Connection url doesn't match the format 's7://{host|ip}/{rack}/{slot}'");
        }
        String host = matcher.group(1);
        int rack = Integer.valueOf(matcher.group(2));
        int slot = Integer.valueOf(matcher.group(3));
        return new S7PlcConnection(host, rack, slot);
    }

    @Override
    public PlcConnection connect(String url, PlcAuthentication authentication) throws PlcConnectionException {
        throw new PlcConnectionException("Basic S7 connections don't support authentication.");
    }

    public static void main(String[] args) throws Exception {
        S7PlcConnection connection = new S7PlcConnection("192.168.0.1", 0, 0);
        connection.connect();

        while (true) {
            Address address = connection.parseAddress("INPUTS/0");
            PlcReadRequest readRequest = new PlcReadRequest(Datatype.BYTE, address);
            PlcReadResponse readResponse = connection.read(readRequest).get();
            byte[] data = (byte[]) readResponse.getValue();
            System.out.println(Arrays.toString(data));

            /*
            ReadVarParameter readVarParameter = new ReadVarParameter();
            readVarParameter.addRequestItem(new S7AnyReadVarRequestItem(
                SpecificationType.VARIABLE_SPECIFICATION, MemoryArea.INPUTS, TransportSize.BYTE, (short) 1, (short) 0, (short) 0, (byte) 0));

            S7RequestMessage readRequest = new S7RequestMessage(MessageType.JOB, (short) 0,
                Collections.singletonList(readVarParameter), Collections.emptyList());*/

            /*
            if (response instanceof S7ResponseMessage) {
                S7ResponseMessage s7ResponseMessage = (S7ResponseMessage) response;
                S7AnyReadVarPayload readVarPayload = s7ResponseMessage.getS7Payload(S7AnyReadVarPayload.class);
                if (readVarPayload != null) {
                    System.out.println(Arrays.toString(readVarPayload.getData()));
                }
            }*/
            Thread.sleep(10);
        }
    }

}
