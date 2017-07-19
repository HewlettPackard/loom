/*******************************************************************************
 * (c) Copyright 2017 Hewlett Packard Enterprise Development LP Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You
 * may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
package com.hp.hpl.loom.manager.query;



/**
 * Functional interface that applies a function to 4 input parameters to obtain an Output as a
 * result.
 *
 * @param <Input1> First input to the function
 * @param <Input2> Second input to the function
 * @param <Input3> Third input to the function
 * @param <Input4> Fourth input to the function
 * @param <Output> Output of the function
 */
@FunctionalInterface
public interface QuadFunction<Input1, Input2, Input3, Input4, Output> {

    /**
     * Quad function interface that takes 4 inputs.
     *
     * @param in1 input1
     * @param in2 input2
     * @param in3 input3
     * @param in4 input4
     * @return the output after performing the quadFunction
     */
    Output apply(Input1 in1, Input2 in2, Input3 in3, Input4 in4);

    // default <V> QuadFunction<Input1, Input2, Input3, Input4, V> andThen(
    // final Function<? super Output, ? extends V> after) {
    // Objects.requireNonNull(after);
    // return (final Input1 in1, final Input2 in2, final Input3 in3, final Input4 in4) ->
    // after.apply(apply(in1, in2,
    // in3, in4));
    // }


}
