/**
 * Copyright (C) 2014 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stratio.sparkta.plugin.operator.median

import java.io.{Serializable => JSerializable}

import scala.util.Try

import com.stratio.sparkta.sdk.{TypeOp, WriteOp, Operator}
import com.stratio.sparkta.sdk.ValidatingPropertyMap._
import breeze.stats._
import breeze.linalg._


class MedianOperator(properties: Map[String, JSerializable]) extends Operator(properties) {

  override val typeOp = Some(TypeOp.Double)

  private val inputField = if(properties.contains("inputField")) Some(properties.getString("inputField")) else None

  override val key : String = "median_" + {
    if(inputField.isDefined) inputField.get else "undefined"
  }

  override val writeOperation = WriteOp.Median

  override def processMap(inputFields: Map[String, JSerializable]): Option[Number] = {
    if ((inputField.isDefined) && (inputFields.contains(inputField.get)))
      getNumberFromSerializable(inputFields.get(inputField.get).get)
    else None
  }

  override def processReduce(values: Iterable[Option[Any]]): Option[Double] = {
    val valuesFiltered = values.flatten
    valuesFiltered.size match {
      case (nz) if (nz != 0) =>
        Some(median(DenseVector(valuesFiltered.map(_.asInstanceOf[Number].doubleValue()).toArray)))
      case _ => MedianOperator.SOME_ZERO
    }
  }
}

private object MedianOperator {
  val SOME_ZERO = Some(0d)
}
