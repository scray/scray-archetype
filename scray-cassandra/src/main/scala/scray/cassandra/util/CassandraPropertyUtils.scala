// See the LICENCE.txt file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package scray.cassandra.util

import com.twitter.storehaus.cassandra.cql.CQLCassandraConfiguration.StoreHost
import scray.common.properties.ScrayProperties
import scala.collection.JavaConverters.asScalaSetConverter
import java.net.InetSocketAddress
import scray.common.properties.Property
import scray.common.properties.ScrayProperties.Phase
import scray.common.properties.IntProperty
import scray.common.properties.ScrayPropertyRegistration
import scray.common.properties.predefined.CommonCassandraRegistrar
import scray.common.properties.predefined.CommonCassandraLoader
import scray.common.properties.predefined.PredefinedProperties
import scray.common.tools.ScrayCredentials

/**
 * utility functions for configurable Cassandra properties
 */
object CassandraPropertyUtils {

  def getCassandraClusterProperty() : String = ScrayProperties.getPropertyValue(PredefinedProperties.CASSANDRA_QUERY_CLUSTER_NAME)
  
  def getCassandraClusterCredentials() : ScrayCredentials = ScrayProperties.getPropertyValue(PredefinedProperties.CASSANDRA_QUERY_CLUSTER_CREDENTIALS)
  
  def getCassandraHostProperty() : Set[StoreHost] = ScrayProperties.getPropertyValue(PredefinedProperties.CASSANDRA_QUERY_SEED_IPS).
    asScala.toSet[InetSocketAddress].map(inetsocket => StoreHost(s"${inetsocket.getHostString}:${inetsocket.getPort}"))

  def performDefaultPropertySystemInitialization(additionPropertiesToRegister : Set[Property[_, _]] = Set()) : Unit = {
    additionPropertiesToRegister.foreach(ScrayProperties.registerProperty(_))
    ScrayPropertyRegistration.addRegistrar(new CommonCassandraRegistrar)
    ScrayPropertyRegistration.addLoader(new CommonCassandraLoader)
    ScrayPropertyRegistration.performPropertySetup
    
  }
}
