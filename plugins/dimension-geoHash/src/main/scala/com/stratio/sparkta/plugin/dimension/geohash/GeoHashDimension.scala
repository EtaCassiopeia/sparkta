/**
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.sparkta.plugin.dimension.geohash

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.github.davidmoten.geo.{GeoHash, LatLong}

import com.stratio.sparkta.plugin.dimension.geohash.GeoHashDimension._
import com.stratio.sparkta.sdk.{BucketType, Bucketer, TypeOp}

/**
 *
 * Aggregation by geoposition
 *
 * Supported buckets by geoHash length:
 * 1 - 5,009.4km x 4,992.6km
 * 2 - 51,252.3km x 624.1km
 * 3 - 156.5km x 156km
 * 4 - 39.1km x 19.5km
 * 5 - 4.9km x 4.9km
 * 6 - 1.2km x 609.4m
 * 7 - 152.9m x 152.4m
 * 8 - 38.2m x 19m
 * 9 - 4.8m x 4.8m
 * 10 - 1.2m x 59.5cm
 * 11 - 14.9cm x 14.9cm
 * 12 - 3.7cm x 1.9cm
 *
 */
case class GeoHashDimension(props: Map[String, JSerializable]) extends Bucketer with SLF4JLogging {

  def this() {
    this(Map())
  }

  override val properties: Map[String, JSerializable] = props

  override val defaultTypeOperation = TypeOp.ArrayDouble

  override val bucketTypes: Map[String, BucketType] =
    Map(
      Precision1Name -> getPrecision(Precision1Name, getTypeOperation(Precision1Name)),
      Precision2Name -> getPrecision(Precision2Name, getTypeOperation(Precision2Name)),
      Precision3Name -> getPrecision(Precision3Name, getTypeOperation(Precision3Name)),
      Precision4Name -> getPrecision(Precision4Name, getTypeOperation(Precision4Name)),
      Precision5Name -> getPrecision(Precision5Name, getTypeOperation(Precision5Name)),
      Precision6Name -> getPrecision(Precision6Name, getTypeOperation(Precision6Name)),
      Precision7Name -> getPrecision(Precision7Name, getTypeOperation(Precision7Name)),
      Precision8Name -> getPrecision(Precision8Name, getTypeOperation(Precision8Name)),
      Precision9Name -> getPrecision(Precision9Name, getTypeOperation(Precision9Name)),
      Precision10Name -> getPrecision(Precision10Name, getTypeOperation(Precision10Name)),
      Precision11Name -> getPrecision(Precision11Name, getTypeOperation(Precision11Name)),
      Precision12Name -> getPrecision(Precision12Name, getTypeOperation(Precision12Name)))

  override def bucket(value: JSerializable): Map[BucketType, JSerializable] = {
    //TODO temporal data treatment
    try {
      if (value.asInstanceOf[Option[_]] != None) {
        bucketTypes.map(bucketType => {
          //TODO temporal data treatment
          val latLongString = value.asInstanceOf[Option[_]].get.asInstanceOf[String].split("__")
          if (latLongString.size != 0) {
            val latDouble = latLongString(0).toDouble
            val longDouble = latLongString(1).toDouble
            bucketType._2 -> GeoHashDimension.bucket(latDouble, longDouble, bucketType._2)
          } else (bucketType._2 -> "")
        })
      } else {
        val defaultPrecision = getPrecision(Precision3Name, getTypeOperation(Precision3Name))
        Map(defaultPrecision -> GeoHashDimension.bucket(0, 0, defaultPrecision))
      }
    }
    catch {
      case aobe: ArrayIndexOutOfBoundsException => {
        log.error("geo problem")
        Map()
      }

      case cce: ClassCastException => {
        log.error("Error parsing " + value + " .")
        throw cce
      }
    }
  }
}

object GeoHashDimension {

  final val Precision1Name = "precision1"
  final val Precision2Name = "precision2"
  final val Precision3Name = "precision3"
  final val Precision4Name = "precision4"
  final val Precision5Name = "precision5"
  final val Precision6Name = "precision6"
  final val Precision7Name = "precision7"
  final val Precision8Name = "precision8"
  final val Precision9Name = "precision9"
  final val Precision10Name = "precision10"
  final val Precision11Name = "precision11"
  final val Precision12Name = "precision12"

  //scalastyle:off
  def bucket(lat: Double, long: Double, bucketType: BucketType): JSerializable = {
    bucketType match {
      case p if p.id == Precision1Name => decodeHash(GeoHash.encodeHash(lat, long, 1))
      case p if p.id == Precision2Name => decodeHash(GeoHash.encodeHash(lat, long, 2))
      case p if p.id == Precision3Name => decodeHash(GeoHash.encodeHash(lat, long, 3))
      case p if p.id == Precision4Name => decodeHash(GeoHash.encodeHash(lat, long, 4))
      case p if p.id == Precision5Name => decodeHash(GeoHash.encodeHash(lat, long, 5))
      case p if p.id == Precision6Name => decodeHash(GeoHash.encodeHash(lat, long, 6))
      case p if p.id == Precision7Name => decodeHash(GeoHash.encodeHash(lat, long, 7))
      case p if p.id == Precision8Name => decodeHash(GeoHash.encodeHash(lat, long, 8))
      case p if p.id == Precision9Name => decodeHash(GeoHash.encodeHash(lat, long, 9))
      case p if p.id == Precision10Name => decodeHash(GeoHash.encodeHash(lat, long, 10))
      case p if p.id == Precision11Name => decodeHash(GeoHash.encodeHash(lat, long, 11))
      case p if p.id == Precision12Name => decodeHash(GeoHash.encodeHash(lat, long, 12))
    }
  }

  //scalastyle:on

  def decodeHash(geoLocHash: String): JSerializable = {
    val geoDecoded: LatLong = GeoHash.decodeHash(geoLocHash)
    val (latitude, longitude) = (geoDecoded.getLat, geoDecoded.getLon)
    Seq(longitude, latitude).asInstanceOf[JSerializable]
  }
}
