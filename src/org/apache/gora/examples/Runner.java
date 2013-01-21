/**
 *Licensed to the Apache Software Foundation (ASF) under one
 *or more contributor license agreements.  See the NOTICE file
 *distributed with this work for additional information
 *regarding copyright ownership.  The ASF licenses this file
 *to you under the Apache License, Version 2.0 (the"
 *License"); you may not use this file except in compliance
 *with the License.  You may obtain a copy of the License at
 *
  * http://www.apache.org/licenses/LICENSE-2.0
 * 
 *Unless required by applicable law or agreed to in writing, software
 *distributed under the License is distributed on an "AS IS" BASIS,
 *WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *See the License for the specific language governing permissions and
 *limitations under the License.
 */
package org.apache.gora.examples;

import java.util.HashSet;

import org.apache.avro.util.Utf8;
import org.apache.gora.cassandra.store.CassandraStore;
import org.apache.gora.dynamodb.store.DynamoDBStore;
import org.apache.gora.examples.generated.User;
import org.apache.gora.persistency.Persistent;
import org.apache.gora.store.DataStore;
import org.apache.gora.store.DataStoreFactory;
import org.apache.gora.util.GoraException;
import org.apache.hadoop.conf.Configuration;

/**
 * @author renatomarroquin
 *
 */
public class Runner {

  /**
   * Data store to handle user storage
   */
  protected static DataStore<String,User> userStore;
  
  private static Configuration conf;

  protected static HashSet<DataStore> dataStores = new HashSet();

  protected static Class<? extends DataStore> dataStoreClass;

  /**
   * @param args
   */
  public static void main(String[] args) {

    String dataStoreName = "Cassandra";
    dataStoreClass = getSpecificDataStore(dataStoreName);
    try {
      userStore = createDataStore(String.class, User.class);
      User usr = userStore.get("pmcfadin");
      if(usr != null)
        System.out.println(usr.getFirstname());
      else
        System.out.println("Null object.");

      usr = new User();
      usr.setFirstname(new Utf8("Renato"));
      userStore.put("renatoj.marroquin", usr);

      usr = userStore.get("renatoj.marroquin");
      System.out.println(usr.getFirstname());

      userStore.flush();
      userStore.close();

    } catch (GoraException e) {
      e.printStackTrace();
    }
  }

  /**
   * Creates a generic data store using the data store class set using the class property
   * @param keyClass
   * @param persistentClass
   * @return
   * @throws GoraException
   */
  @SuppressWarnings("unchecked")
  public static <K, T extends Persistent> DataStore<K,T>
    createDataStore(Class<K> keyClass, Class<T> persistentClass) throws GoraException {
    DataStoreFactory.createProps();
    DataStore<K,T> dataStore = 
        DataStoreFactory.createDataStore((Class<? extends DataStore<K,T>>)dataStoreClass, 
                                          keyClass, persistentClass,
                                          conf);
    if (dataStore != null)
      dataStores.add(dataStore);

    return dataStore;
  }

  /**
   * Returns the specific type of class for the requested data store
   * @param pDataStoreName
   * @return
   */
  private static Class<? extends DataStore> getSpecificDataStore(String pDataStoreName){
    if (pDataStoreName == "Cassandra")
        return CassandraStore.class;
    if (pDataStoreName == "DynamoDB")
        return DynamoDBStore.class;
    return null;
  }
}