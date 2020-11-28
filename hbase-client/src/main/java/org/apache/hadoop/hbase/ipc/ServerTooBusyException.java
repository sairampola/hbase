/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.hbase.ipc;

import java.net.InetSocketAddress;

import org.apache.hadoop.hbase.DoNotRetryIOException;
import org.apache.hadoop.hbase.classification.InterfaceAudience;
import org.apache.hadoop.hbase.classification.InterfaceStability;
import org.apache.hadoop.hbase.net.Address;

/**
 * Throw this in rpc call if there are too many pending requests for one region server
 */
@InterfaceAudience.Public
@InterfaceStability.Evolving
public class ServerTooBusyException extends DoNotRetryIOException {

  private static final long serialVersionUID = 1L;

  public ServerTooBusyException(Address address, long count){
    super("There are "+count+" concurrent rpc requests for "+address);
  }

  @Deprecated
  public ServerTooBusyException(InetSocketAddress address, long count){
    super("There are "+count+" concurrent rpc requests for "+address);
  }

}