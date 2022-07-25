/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.constraintlayout.core.state

import androidx.constraintlayout.core.motion.utils.TypedBundle
import androidx.constraintlayout.core.motion.utils.TypedValues.MotionType
import androidx.constraintlayout.core.parser.*
import androidx.constraintlayout.core.state.helpers.*
import androidx.constraintlayout.core.widgets.ConstraintWidget

object ConstraintSetParser {
    private const val PARSER_DEBUG = false
    //==================== end Motion Scene =========================
    /**
     * Parse and populate a transition
     *
     * @param content    JSON string to parse
     * @param transition The Transition to be populated
     * @param state      @TODO what is this
     */
    fun parseJSON(content: String, transition: Transition, state: Int) {
        try {
            val json: CLObject = CLParser.Companion.parse(content)
            val elements: ArrayList<String> = json.names() ?: return
            for (elementName in elements) {
                val base_element: CLElement = json.get(elementName)!!
                if (base_element is CLObject) {
                    val customProperties: CLObject? = base_element.getObjectOrNull("custom")
                    if (customProperties != null) {
                        val properties: ArrayList<String> = customProperties.names()
                        for (property in properties) {
                            val value: CLElement = customProperties.get(property!!)!!
                            if (value is CLNumber) {
                                transition.addCustomFloat(
                                    state,
                                    elementName,
                                    property,
                                    value.float
                                )
                            } else if (value is CLString) {
                                val color = parseColorString(value.content())
                                if (color != -1L) {
                                    transition.addCustomColor(
                                        state,
                                        elementName, property, color.toInt()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: CLParsingException) {
            println("Error parsing JSON $e")
        }
    }

    /**
     * Parse and build a motionScene
     *
     * @Todo this should be in a MotionScene / MotionSceneParser
     */
    fun parseMotionSceneJSON(scene: CoreMotionScene, content: String) {
        try {
            val json: CLObject = CLParser.Companion.parse(content)
            val elements: ArrayList<String> = json.names() ?: return
            for (elementName in elements) {
                val element: CLElement = json.get(elementName)!!
                if (element is CLObject) {
                    val clObject = element
                    when (elementName) {
                        "ConstraintSets" -> parseConstraintSets(scene, clObject)
                        "Transitions" -> parseTransitions(scene, clObject)
                        "Header" -> parseHeader(scene, clObject)
                    }
                }
            }
        } catch (e: CLParsingException) {
            println("Error parsing JSON $e")
        }
    }

    /**
     * Parse ConstraintSets and populate MotionScene
     */
    @Throws(CLParsingException::class)
    fun parseConstraintSets(
        scene: CoreMotionScene,
        json: CLObject
    ) {
        val constraintSetNames: ArrayList<String> = json.names() ?: return
        for (csName in constraintSetNames) {
            val constraintSet: CLObject = json.getObject(csName!!)
            var added = false
            val ext = constraintSet.getStringOrNull("Extends")
            if (!ext.isNullOrEmpty()) {
                val base = scene.getConstraintSet(ext) ?: continue
                val baseJson: CLObject = CLParser.Companion.parse(base)
                val widgetsOverride: ArrayList<String> = constraintSet.names() ?: continue
                for (widgetOverrideName in widgetsOverride) {
                    val value: CLElement = constraintSet.get(widgetOverrideName!!)!!
                    if (value is CLObject) {
                        override(baseJson, widgetOverrideName, value)
                    }
                }
                scene.setConstraintSetContent(csName, baseJson.toJSON())
                added = true
            }
            if (!added) {
                scene.setConstraintSetContent(csName, constraintSet.toJSON())
            }
        }
    }

    @Throws(CLParsingException::class)
    fun override(
        baseJson: CLObject?,
        name: String?, overrideValue: CLObject
    ) {
        if (!baseJson!!.has(name!!)) {
            baseJson.put(name!!, overrideValue)
        } else {
            val base: CLObject = baseJson.getObject(name!!)
            val keys: ArrayList<String> = overrideValue.names()
            for (key in keys) {
                if (key != "clear") {
                    base.put(key, overrideValue.get(key))
                    continue
                }
                val toClear: CLArray = overrideValue.getArray("clear")
                for (i in 0 until toClear.size()) {
                    val clearedKey: String = toClear.getStringOrNull(i) ?: continue
                    when (clearedKey) {
                        "dimensions" -> {
                            base.remove("width")
                            base.remove("height")
                        }

                        "constraints" -> {
                            base.remove("start")
                            base.remove("end")
                            base.remove("top")
                            base.remove("bottom")
                            base.remove("baseline")
                            base.remove("center")
                            base.remove("centerHorizontally")
                            base.remove("centerVertically")
                        }

                        "transforms" -> {
                            base.remove("visibility")
                            base.remove("alpha")
                            base.remove("pivotX")
                            base.remove("pivotY")
                            base.remove("rotationX")
                            base.remove("rotationY")
                            base.remove("rotationZ")
                            base.remove("scaleX")
                            base.remove("scaleY")
                            base.remove("translationX")
                            base.remove("translationY")
                        }

                        else -> base.remove(clearedKey)
                    }
                }
            }
        }
    }

    /**
     * Parse the Transition
     */
    @Throws(CLParsingException::class)
    fun parseTransitions(scene: CoreMotionScene, json: CLObject) {
        val elements: ArrayList<String> = json.names() ?: return
        for (elementName in elements) {
            scene.setTransitionContent(elementName, json.getObject(elementName!!).toJSON())
        }
    }

    /**
     * Used to parse for "export"
     */
    fun parseHeader(scene: CoreMotionScene, json: CLObject) {
        val name = json.getStringOrNull("export")
        if (name != null) {
            scene.setDebugName(name)
        }
    }

    /**
     * Top leve parsing of the json ConstraintSet supporting
     * "Variables", "Helpers", "Generate", guidelines, and barriers
     *
     * @param content         the JSON string
     * @param state           the state to populate
     * @param layoutVariables the variables to override
     */
    @Throws(CLParsingException::class)
    fun parseJSON(
        content: String, state: State,
        layoutVariables: LayoutVariables
    ) {
        try {
            val json: CLObject = CLParser.Companion.parse(content)
            val elements: ArrayList<String> = json.names() ?: return
            for (elementName in elements) {
                val element: CLElement = json.get(elementName)!!
                if (PARSER_DEBUG) {
                    println(
                        "[" + elementName + "] = " + element
                                + " > " + element.container
                    )
                }
                when (elementName) {
                    "Variables" -> if (element is CLObject) {
                        parseVariables(state, layoutVariables, element)
                    }

                    "Helpers" -> if (element is CLArray) {
                        parseHelpers(state, layoutVariables, element)
                    }

                    "Generate" -> if (element is CLObject) {
                        parseGenerate(state, layoutVariables, element)
                    }

                    else -> if (element is CLObject) {
                        val type = lookForType(element)
                        if (type != null) {
                            when (type) {
                                "hGuideline" -> parseGuidelineParams(
                                    ConstraintWidget.Companion.HORIZONTAL,
                                    state,
                                    elementName,
                                    element
                                )

                                "vGuideline" -> parseGuidelineParams(
                                    ConstraintWidget.Companion.VERTICAL,
                                    state,
                                    elementName,
                                    element
                                )

                                "barrier" -> parseBarrier(state, elementName, element)
                                "vChain", "hChain" -> parseChainType(
                                    type,
                                    state,
                                    elementName,
                                    layoutVariables,
                                    element
                                )
                            }
                        } else {
                            parseWidget(
                                state, layoutVariables,
                                elementName, element
                            )
                        }
                    } else if (element is CLNumber) {
                        layoutVariables.put(elementName, element.getInt())
                    }
                }
            }
        } catch (e: CLParsingException) {
            println("Error parsing JSON $e")
        }
    }

    @Throws(CLParsingException::class)
    private fun parseVariables(
        state: State,
        layoutVariables: LayoutVariables,
        json: CLObject
    ) {
        val elements: ArrayList<String> = json.names()
        for (elementName in elements) {
            val element: CLElement = json.get(elementName)!!
            if (element is CLNumber) {
                layoutVariables.put(elementName, element.getInt())
            } else if (element is CLObject) {
                val obj = element
                var arrayIds: ArrayList<String>
                if (obj.has("from") && obj.has("to")) {
                    val from = layoutVariables[obj.get("from")!!]
                    val to = layoutVariables[obj.get("to")!!]
                    val prefix: String = obj.getStringOrNull("prefix")!!
                    val postfix: String = obj.getStringOrNull("postfix")!!
                    layoutVariables.put(elementName, from, to, 1f, prefix, postfix)
                } else if (obj.has("from") && obj.has("step")) {
                    val start = layoutVariables[obj.get("from")!!]
                    val increment = layoutVariables[obj.get("step")!!]
                    layoutVariables.put(elementName, start, increment)
                } else if (obj.has("ids")) {
                    val ids: CLArray = obj.getArray("ids")
                    arrayIds = ArrayList()
                    for (i in 0 until ids.size()) {
                        arrayIds.add(ids.getString(i)!!)
                    }
                    layoutVariables.put(elementName, arrayIds)
                } else if (obj.has("tag")) {
                    arrayIds = state.getIdsForTag(obj.getString("tag"))
                    layoutVariables.put(elementName, arrayIds)
                }
            }
        }
    }

    /**
     * parse the Design time elements.
     *
     * @param content the json
     * @param list    output the list of design elements
     */
    @Throws(CLParsingException::class)
    fun parseDesignElementsJSON(
        content: String, list: ArrayList<DesignElement?>
    ) {
        val json: CLObject = CLParser.Companion.parse(content)
        var elements: ArrayList<String> = json.names() ?: return
        for (i in elements.indices) {
            val elementName: String = elements.get(i)
            val element: CLElement = json.get(elementName)!!
            if (PARSER_DEBUG) {
                println("[" + element + "] " + element::class)
            }
            when (elementName) {
                "Design" -> {
                    if (element !is CLObject) {
                        return
                    }
                    elements = element.names()
                    var j = 0
                    while (j < elements.size) {
                        val designElementName: String = elements.get(j)
                        val designElement = element.get(designElementName) as CLObject
                        println("element found $designElementName")
                        val type = designElement.getStringOrNull("type")
                        if (type != null) {
                            val parameters: HashMap<String?, String?> = HashMap()
                            val size: Int = designElement.size()
                            var k = 0
                            while (k < size) {
                                val key = designElement.get(j) as CLKey
                                val paramName: String = key.content()
                                val paramValue = key.value?.content()
                                if (paramValue != null) {
                                    parameters.put(paramName, paramValue)
                                }
                                k++
                            }
                            list.add(DesignElement(elementName, type, parameters))
                        }
                        j++
                    }
                }
            }
            break
        }
    }

    @Throws(CLParsingException::class)
    fun parseHelpers(
        state: State,
        layoutVariables: LayoutVariables,
        element: CLArray
    ) {
        for (i in 0 until element.size()) {
            val helper: CLElement = element.get(i)
            if (helper is CLArray) {
                val array = helper
                if (array.size() > 1) {
                    when (array.getString(0)) {
                        "hChain" -> parseChain(ConstraintWidget.Companion.HORIZONTAL, state, layoutVariables, array)
                        "vChain" -> parseChain(ConstraintWidget.Companion.VERTICAL, state, layoutVariables, array)
                        "hGuideline" -> parseGuideline(ConstraintWidget.Companion.HORIZONTAL, state, array)
                        "vGuideline" -> parseGuideline(ConstraintWidget.Companion.VERTICAL, state, array)
                    }
                }
            }
        }
    }

    @Throws(CLParsingException::class)
    fun parseGenerate(
        state: State,
        layoutVariables: LayoutVariables,
        json: CLObject
    ) {
        val elements: ArrayList<String> = json.names() ?: return
        for (elementName in elements) {
            val element: CLElement = json.get(elementName!!)!!
            val arrayIds: ArrayList<String>? = layoutVariables.getList(elementName)
            if (arrayIds != null && element is CLObject) {
                for (id in arrayIds) {
                    parseWidget(state, layoutVariables, id, element)
                }
            }
        }
    }

    @Throws(CLParsingException::class)
    fun parseChain(
        orientation: Int, state: State,
        margins: LayoutVariables, helper: CLArray
    ) {
        val chain: ChainReference? =
            if (orientation == ConstraintWidget.Companion.HORIZONTAL) state.horizontalChain() else state.verticalChain()
        val refs: CLElement = helper.get(1)
        if (refs !is CLArray || refs.size() < 1) {
            return
        }
        for (i in 0 until refs.size()) {
            chain?.add(refs.getString(i)!!)
        }
        if (helper.size() > 2) { // we have additional parameters
            val params: CLElement = helper.get(2)
            if (params !is CLObject) {
                return
            }
            val constraints: ArrayList<String> = params.names()
            for (constraintName in constraints) {
                when (constraintName) {
                    "style" -> {
                        val styleObject: CLElement = params.get(constraintName)!!
                        var styleValue: String?
                        if (styleObject is CLArray && styleObject.size() > 1) {
                            styleValue = styleObject.getString(0)
                            val biasValue: Float = styleObject.getFloat(1)
                            chain!!.bias(biasValue)
                        } else {
                            styleValue = styleObject.content()
                        }
                        when (styleValue) {
                            "packed" -> chain!!.style(State.Chain.PACKED)
                            "spread_inside" -> chain!!.style(State.Chain.SPREAD_INSIDE)
                            else -> chain!!.style(State.Chain.SPREAD)
                        }
                    }

                    else -> parseConstraint(
                        state,
                        margins,
                        params,
                        chain as ConstraintReference?,
                        constraintName
                    )
                }
            }
        }
    }

    private fun toPix(state: State, dp: Float): Float {
        return state.dpToPixel!!.toPixels(dp)
    }

    /**
     * Support parsing Chain in the following manner
     * chainId : {
     * type:'hChain'  // or vChain
     * contains: ['id1', 'id2', 'id3' ]
     * contains: [['id', weight, marginL ,marginR], 'id2', 'id3' ]
     * start: ['parent', 'start',0],
     * end: ['parent', 'end',0],
     * top: ['parent', 'top',0],
     * bottom: ['parent', 'bottom',0],
     * style: 'spread'
     * }
     *
     * @throws CLParsingException
     */
    @Throws(CLParsingException::class)
    private fun parseChainType(
        orientation: String,
        state: State,
        chainName: String,
        margins: LayoutVariables,
        `object`: CLObject
    ) {
        val chain: ChainReference = if (orientation[0] == 'h') state.horizontalChain() else state.verticalChain()
        chain.key = (chainName)
        for (params in `object`.names()) {
            when (params) {
                "contains" -> {
                    val refs: CLElement = `object`.get(params)!!
                    if (refs !is CLArray || refs.size() < 1) {
                        println(
                            chainName + " contains should be an array \"" + refs.content()
                                    + "\""
                        )
                        return
                    }
                    var i = 0
                    while (i < refs.size()) {
                        val chainElement: CLElement = refs.get(i)
                        if (chainElement is CLArray) {
                            val array = chainElement
                            if (array.size() > 0) {
                                val id: String = array.get(0).content()
                                var weight = Float.NaN
                                var preMargin = Float.NaN
                                var postMargin = Float.NaN
                                when (array.size()) {
                                    2 -> weight = array.getFloat(1)
                                    3 -> {
                                        weight = array.getFloat(1)
                                        run {
                                            preMargin = toPix(state, array.getFloat(2))
                                            postMargin = preMargin
                                        }
                                    }

                                    4 -> {
                                        weight = array.getFloat(1)
                                        preMargin = toPix(state, array.getFloat(2))
                                        postMargin = toPix(state, array.getFloat(3))
                                    }
                                }
                                chain!!.addChainElement(id, weight, preMargin, postMargin)
                            }
                        } else {
                            chain.add(chainElement.content())
                        }
                        i++
                    }
                }

                "start", "end", "top", "bottom", "left", "right" -> parseConstraint(
                    state,
                    margins,
                    `object`,
                    chain,
                    params
                )

                "style" -> {
                    val styleObject: CLElement = `object`.get(params)!!
                    var styleValue: String?
                    if (styleObject is CLArray && styleObject.size() > 1) {
                        styleValue = styleObject.getString(0)
                        val biasValue: Float = styleObject.getFloat(1)
                        chain!!.bias(biasValue)
                    } else {
                        styleValue = styleObject.content()
                    }
                    when (styleValue) {
                        "packed" -> chain!!.style(State.Chain.PACKED)
                        "spread_inside" -> chain!!.style(State.Chain.SPREAD_INSIDE)
                        else -> chain!!.style(State.Chain.SPREAD)
                    }
                }
            }
        }
    }

    @Throws(CLParsingException::class)
    fun parseGuideline(
        orientation: Int,
        state: State, helper: CLArray
    ) {
        val params: CLElement = helper.get(1)
        if (params !is CLObject) {
            return
        }
        val guidelineId: String = params.getStringOrNull("id") ?: return
        parseGuidelineParams(orientation, state, guidelineId, params)
    }

    @Throws(CLParsingException::class)
    fun parseGuidelineParams(
        orientation: Int,
        state: State,
        guidelineId: String,
        params: CLObject
    ) {
        val constraints: ArrayList<String> = params.names() ?: return
        val reference = state.constraints(guidelineId)
        if (orientation == ConstraintWidget.Companion.HORIZONTAL) {
            state.horizontalGuideline(guidelineId)
        } else {
            state.verticalGuideline(guidelineId)
        }
        val guidelineReference = reference!!.facade as GuidelineReference
        for (constraintName in constraints) {
            when (constraintName) {
                "start" -> {
                    val margin = state.convertDimension(params.getFloat(constraintName))
                    guidelineReference.start(state.dpToPixel!!.toPixels(margin.toFloat()))
                }

                "end" -> {
                    val margin = state.convertDimension(params.getFloat(constraintName))
                    guidelineReference.end(state.dpToPixel!!.toPixels(margin.toFloat()))
                }

                "percent" -> guidelineReference.percent(params.getFloat(constraintName))
            }
        }
    }

    @Throws(CLParsingException::class)
    fun parseBarrier(
        state: State,
        elementName: String?, element: CLObject
    ) {
        val reference = state.barrier(elementName, State.Direction.END)
        val constraints: ArrayList<String> = element.names() ?: return
        for (constraintName in constraints) {
            when (constraintName) {
                "direction" -> {
                    when (element.getString(constraintName)) {
                        "start" -> reference!!.setBarrierDirection(State.Direction.START)
                        "end" -> reference!!.setBarrierDirection(State.Direction.END)
                        "left" -> reference!!.setBarrierDirection(State.Direction.LEFT)
                        "right" -> reference!!.setBarrierDirection(State.Direction.RIGHT)
                        "top" -> reference!!.setBarrierDirection(State.Direction.TOP)
                        "bottom" -> reference!!.setBarrierDirection(State.Direction.BOTTOM)
                    }
                }

                "margin" -> {
                    val margin: Float = element.getFloatOrNaN(constraintName)
                    if (!margin.isNaN()) {
                        reference!!.margin(margin) // TODO is this a bug
                    }
                }

                "contains" -> {
                    val list = element.getArrayOrNull(constraintName)
                    if (list != null) {
                        var j = 0
                        while (j < list.size()) {
                            val elementNameReference: String = list.get(j).content()
                            val elementReference = state.constraints(elementNameReference)
                            if (PARSER_DEBUG) {
                                println(
                                    "Add REFERENCE "
                                            + "(\$elementNameReference = \$elementReference) "
                                            + "TO BARRIER "
                                )
                            }
                            reference?.add(elementReference!!)
                            j++
                        }
                    }
                }
            }
        }
    }

    @Throws(CLParsingException::class)
    fun parseWidget(
        state: State,
        layoutVariables: LayoutVariables,
        elementName: String,
        element: CLObject
    ) {
        var value: Float
        val reference = state.constraints(elementName)
        if (reference?.width == null) {
            // Default to Wrap when the Dimension has not been assigned
            reference!!.width = Dimension.Companion.createWrap()
        }
        if (reference?.height == null) {
            // Default to Wrap when the Dimension has not been assigned
            reference!!.height = Dimension.Companion.createWrap()
        }
        val constraints: ArrayList<String> = element.names() ?: return
        for (constraintName in constraints) {
            when (constraintName) {
                "width" -> reference!!.width = parseDimension(
                    element,
                    constraintName, state, state.dpToPixel
                )

                "height" -> reference!!.height = parseDimension(
                    element,
                    constraintName, state, state.dpToPixel
                )

                "center" -> {
                    val target: String = element.getString(constraintName)!!
                    var targetReference: ConstraintReference?
                    targetReference = if (target == "parent") {
                        state.constraints(State.Companion.PARENT)
                    } else {
                        state.constraints(target)
                    }
                    reference!!.startToStart(targetReference)
                    reference.endToEnd(targetReference)
                    reference.topToTop(targetReference)
                    reference.bottomToBottom(targetReference)
                }

                "centerHorizontally" -> {
                    val target = element.getString(constraintName)
                    val targetReference =
                        if (target == "parent") state.constraints(State.Companion.PARENT) else state.constraints(target!!)
                    reference!!.startToStart(targetReference)
                    reference.endToEnd(targetReference)
                }

                "centerVertically" -> {
                    val target = element.getString(constraintName)
                    val targetReference =
                        if (target == "parent") state.constraints(State.Companion.PARENT) else state.constraints(target!!)
                    reference!!.topToTop(targetReference)
                    reference.bottomToBottom(targetReference)
                }

                "alpha" -> {
                    value = layoutVariables[element.get(constraintName)!!]
                    reference!!.alpha(value)
                }

                "scaleX" -> {
                    value = layoutVariables[element.get(constraintName)!!]
                    reference!!.scaleX(value)
                }

                "scaleY" -> {
                    value = layoutVariables[element.get(constraintName)!!]
                    reference!!.scaleY(value)
                }

                "translationX" -> {
                    value = layoutVariables[element.get(constraintName)!!]
                    reference!!.translationX(value)
                }

                "translationY" -> {
                    value = layoutVariables[element.get(constraintName)!!]
                    reference!!.translationY(value)
                }

                "translationZ" -> {
                    value = layoutVariables[element.get(constraintName)!!]
                    reference!!.translationZ(value)
                }

                "pivotX" -> {
                    value = layoutVariables[element.get(constraintName)!!]
                    reference!!.pivotX(value)
                }

                "pivotY" -> {
                    value = layoutVariables[element.get(constraintName)!!]
                    reference!!.pivotY(value)
                }

                "rotationX" -> {
                    value = layoutVariables[element.get(constraintName)!!]
                    reference!!.rotationX(value)
                }

                "rotationY" -> {
                    value = layoutVariables[element.get(constraintName)!!]
                    reference!!.rotationY(value)
                }

                "rotationZ" -> {
                    value = layoutVariables[element.get(constraintName)!!]
                    reference!!.rotationZ(value)
                }

                "visibility" -> when (element.getString(constraintName)) {
                    "visible" -> reference!!.visibility(ConstraintWidget.Companion.VISIBLE)
                    "invisible" -> reference!!.visibility(ConstraintWidget.Companion.INVISIBLE)
                    "gone" -> reference!!.visibility(ConstraintWidget.Companion.GONE)
                }

                "vBias" -> {
                    value = layoutVariables[element.get(constraintName)!!]
                    reference!!.verticalBias(value)
                }

                "hBias" -> {
                    value = layoutVariables[element.get(constraintName)!!]
                    reference!!.horizontalBias(value)
                }

                "vWeight" -> {
                    value = layoutVariables[element.get(constraintName)!!]
                    reference.verticalChainWeight = (value)
                }

                "hWeight" -> {
                    value = layoutVariables[element.get(constraintName)!!]
                    reference.horizontalChainWeight = (value)
                }

                "custom" -> parseCustomProperties(element, reference, constraintName)
                "motion" -> parseMotionProperties(element.get(constraintName)!!, reference)
                else -> parseConstraint(state, layoutVariables, element, reference, constraintName)
            }
        }
    }

    @Throws(CLParsingException::class)
    fun parseCustomProperties(
        element: CLObject,
        reference: ConstraintReference?,
        constraintName: String?
    ) {
        val json: CLObject = element.getObjectOrNull(constraintName!!) ?: return
        val properties: ArrayList<String> = json.names() ?: return
        for (property in properties) {
            val value: CLElement = json.get(property!!)!!
            if (value is CLNumber) {
                reference!!.addCustomFloat(property, value.float)
            } else if (value is CLString) {
                val it = parseColorString(value.content())
                if (it != -1L) {
                    reference!!.addCustomColor(property, it.toInt())
                }
            }
        }
    }

    private fun indexOf(`val`: String, vararg types: String): Int {
        for (i in types.indices) {
            if (types[i] == `val`) {
                return i
            }
        }
        return -1
    }

    /**
     * parse the motion section of a constraint
     * <pre>
     * csetName: {
     * idToConstrain : {
     * motion: {
     * pathArc : 'startVertical'
     * relativeTo: 'id'
     * easing: 'curve'
     * stagger: '2'
     * quantize: steps or [steps, 'interpolator' phase ]
     * }
     * }
     * }
    </pre> *
     */
    @Throws(CLParsingException::class)
    private fun parseMotionProperties(
        element: CLElement,
        reference: ConstraintReference?
    ) {
        if (element !is CLObject) {
            return
        }
        val obj = element
        val bundle = TypedBundle()
        val constraints: ArrayList<String> = obj.names() ?: return
        for (constraintName in constraints) {
            when (constraintName) {
                "pathArc" -> {
                    val `val`: String = obj.getString(constraintName)!!
                    val ord = indexOf(`val`, "none", "startVertical", "startHorizontal", "flip")
                    if (ord == -1) {
                        println(obj.line.toString() + " pathArc = '" + `val` + "'")
                        break
                    }
                    bundle.add(MotionType.Companion.TYPE_PATHMOTION_ARC, ord)
                }

                "relativeTo" -> bundle.add(
                    MotionType.Companion.TYPE_ANIMATE_RELATIVE_TO,
                    obj.getString(constraintName)
                )

                "easing" -> bundle.add(MotionType.Companion.TYPE_EASING, obj.getString(constraintName))
                "stagger" -> bundle.add(MotionType.Companion.TYPE_STAGGER, obj.getFloat(constraintName))
                "quantize" -> {
                    val quant: CLElement = obj.get(constraintName)!!
                    if (quant is CLArray) {
                        val array = quant
                        val len: Int = array.size()
                        if (len > 0) {
                            bundle.add(MotionType.Companion.TYPE_QUANTIZE_MOTIONSTEPS, array.getInt(0))
                            if (len > 1) {
                                bundle.add(MotionType.Companion.TYPE_QUANTIZE_INTERPOLATOR_TYPE, array.getString(1))
                                if (len > 2) {
                                    bundle.add(MotionType.Companion.TYPE_QUANTIZE_MOTION_PHASE, array.getFloat(2))
                                }
                            }
                        }
                    } else {
                        bundle.add(MotionType.Companion.TYPE_QUANTIZE_MOTIONSTEPS, obj.getInt(constraintName))
                    }
                }
            }
        }
        reference!!.mMotionProperties = bundle
    }

    @Throws(CLParsingException::class)
    fun parseConstraint(
        state: State,
        layoutVariables: LayoutVariables,
        element: CLObject,
        reference: ConstraintReference?,
        constraintName: String?
    ) {
        val constraint = element.getArrayOrNull(constraintName!!)
        if (constraint != null && constraint.size() > 1) {
            val target: String = constraint.getString(0)!!
            val anchor: String = constraint.getStringOrNull(1)!!
            var margin = 0f
            var marginGone = 0f
            if (constraint.size() > 2) {
                val arg2: CLElement = constraint.getOrNull(2)!!
                margin = layoutVariables[arg2]
                margin = state.convertDimension(state.dpToPixel!!.toPixels(margin)).toFloat()
            }
            if (constraint.size() > 3) {
                val arg2: CLElement = constraint.getOrNull(3)!!
                marginGone = layoutVariables[arg2]
                marginGone = state.convertDimension(state.dpToPixel!!.toPixels(margin)).toFloat()
            }
            val targetReference =
                if (target == "parent") state.constraints(State.Companion.PARENT) else state.constraints(target)
            when (constraintName) {
                "circular" -> {
                    val angle = layoutVariables[constraint.get(1)]
                    reference!!.circularConstraint(targetReference, angle, 0f)
                }

                "start" -> when (anchor) {
                    "start" -> reference!!.startToStart(targetReference)
                    "end" -> reference!!.startToEnd(targetReference)
                }

                "end" -> when (anchor) {
                    "start" -> reference!!.endToStart(targetReference)
                    "end" -> reference!!.endToEnd(targetReference)
                }

                "left" -> when (anchor) {
                    "left" -> reference!!.leftToLeft(targetReference)
                    "right" -> reference!!.leftToRight(targetReference)
                }

                "right" -> when (anchor) {
                    "left" -> reference!!.rightToLeft(targetReference)
                    "right" -> reference!!.rightToRight(targetReference)
                }

                "top" -> when (anchor) {
                    "top" -> reference!!.topToTop(targetReference)
                    "bottom" -> reference!!.topToBottom(targetReference)
                }

                "bottom" -> when (anchor) {
                    "top" -> reference!!.bottomToTop(targetReference)
                    "bottom" -> reference!!.bottomToBottom(targetReference)
                }

                "baseline" -> when (anchor) {
                    "baseline" -> {
                        state.baselineNeededFor(reference!!.key)
                        state.baselineNeededFor(targetReference!!.key)
                        reference.baselineToBaseline(targetReference)
                    }

                    "top" -> {
                        state.baselineNeededFor(reference!!.key)
                        state.baselineNeededFor(targetReference!!.key)
                        reference.baselineToTop(targetReference)
                    }

                    "bottom" -> {
                        state.baselineNeededFor(reference!!.key)
                        state.baselineNeededFor(targetReference!!.key)
                        reference.baselineToBottom(targetReference)
                    }
                }
            }
            reference!!.margin(margin).marginGone(marginGone)
        } else {
            val target = element.getStringOrNull(constraintName!!)
            if (target != null) {
                val targetReference =
                    if (target == "parent") state.constraints(State.Companion.PARENT) else state.constraints(target)
                when (constraintName) {
                    "start" -> reference!!.startToStart(targetReference)
                    "end" -> reference!!.endToEnd(targetReference)
                    "top" -> reference!!.topToTop(targetReference)
                    "bottom" -> reference!!.bottomToBottom(targetReference)
                    "baseline" -> {
                        state.baselineNeededFor(reference!!.key)
                        state.baselineNeededFor(targetReference!!.key)
                        reference.baselineToBaseline(targetReference)
                    }
                }
            }
        }
    }

    fun parseDimensionMode(dimensionString: String?): Dimension {
        var dimension: Dimension = Dimension.Companion.createFixed(0)
        when (dimensionString) {
            "wrap" -> dimension = Dimension.Companion.createWrap()
            "preferWrap" -> dimension = Dimension.Companion.createSuggested(Dimension.Companion.WRAP_DIMENSION)
            "spread" -> dimension = Dimension.Companion.createSuggested(Dimension.Companion.SPREAD_DIMENSION)
            "parent" -> dimension = Dimension.Companion.createParent()
            else -> {
                if (dimensionString!!.endsWith("%")) {
                    // parent percent
                    val percentString = dimensionString.substring(0, dimensionString.indexOf('%'))
                    val percentValue = percentString.toFloat() / 100f
                    dimension = Dimension.Companion.createPercent(0, percentValue).suggested(0)
                } else if (dimensionString.contains(":")) {
                    dimension = Dimension.Companion.createRatio(dimensionString)
                        .suggested(Dimension.Companion.SPREAD_DIMENSION)
                }
            }
        }
        return dimension
    }

    @Throws(CLParsingException::class)
    fun parseDimension(
        element: CLObject,
        constraintName: String?,
        state: State,
        dpToPixels: CorePixelDp?
    ): Dimension {
        val dimensionElement: CLElement = element.get(constraintName!!)!!
        var dimension: Dimension = Dimension.Companion.createFixed(0)
        if (dimensionElement is CLString) {
            dimension = parseDimensionMode(dimensionElement.content())
        } else if (dimensionElement is CLNumber) {
            dimension = Dimension.Companion.createFixed(
                state.convertDimension(dpToPixels!!.toPixels(element.getFloat(constraintName!!)))
            )
        } else if (dimensionElement is CLObject) {
            val obj = dimensionElement
            val mode = obj.getStringOrNull("value")
            if (mode != null) {
                dimension = parseDimensionMode(mode)
            }
            val minEl = obj.getOrNull("min")
            if (minEl != null) {
                if (minEl is CLNumber) {
                    val min = minEl.float
                    dimension.min(state.convertDimension(dpToPixels!!.toPixels(min)))
                } else if (minEl is CLString) {
                    dimension.min(Dimension.Companion.WRAP_DIMENSION)
                }
            }
            val maxEl = obj.getOrNull("max")
            if (maxEl != null) {
                if (maxEl is CLNumber) {
                    val max = maxEl.float
                    dimension.max(state.convertDimension(dpToPixels!!.toPixels(max)))
                } else if (maxEl is CLString) {
                    dimension.max(Dimension.Companion.WRAP_DIMENSION)
                }
            }
        }
        return dimension
    }

    /**
     * parse a color string
     *
     * @return -1 if it cannot parse unsigned long
     */
    fun parseColorString(value: String?): Long {
        var str = value
        return if (str!!.startsWith("#")) {
            str = str.substring(1)
            if (str.length == 6) {
                str = "FF$str"
            }
            str.toLong(16)
        } else {
            -1L
        }
    }

    @Throws(CLParsingException::class)
    fun lookForType(element: CLObject): String? {
        val constraints: ArrayList<String> = element.names()
        for (constraintName in constraints) {
            if (constraintName == "type") {
                return element.getString("type")
            }
        }
        return null
    }

    class DesignElement internal constructor(
        var id: String,
        var type: String,
        params: HashMap<String?, String?>
    ) {
        var mParams: HashMap<String?, String?>
        val params: HashMap<String?, String?>
            get() = mParams

        init {
            mParams = params
        }
    }

    /**
     * Provide the storage for managing Variables in the system.
     * When the json has a variable:{   } section this is used.
     */
    class LayoutVariables {
        var mMargins: HashMap<String, Int> = HashMap<String, Int>()
        var mGenerators: HashMap<String, GeneratedValue> = HashMap<String, GeneratedValue>()
        var mArrayIds: HashMap<String, ArrayList<String>> =
            HashMap<String, ArrayList<String>>()

        fun put(elementName: String, element: Int) {
            mMargins.put(elementName, element)
        }

        fun put(elementName: String, start: Float, incrementBy: Float) {
            if (mGenerators.containsKey(elementName)) {
                if (mGenerators.get(elementName) is OverrideValue) {
                    return
                }
            }
            mGenerators.put(elementName, Generator(start, incrementBy))
        }

        fun put(
            elementName: String,
            from: Float,
            to: Float,
            step: Float,
            prefix: String?,
            postfix: String?
        ) {
            if (mGenerators.containsKey(elementName)) {
                if (mGenerators.get(elementName) is OverrideValue) {
                    return
                }
            }
            val generator = FiniteGenerator(from, to, step, prefix, postfix)
            mGenerators.put(elementName, generator)
            mArrayIds.put(elementName, generator.array())
        }

        /**
         * insert an override variable
         *
         * @param elementName the name
         * @param value       the value a float
         */
        fun putOverride(elementName: String, value: Float) {
            val generator: GeneratedValue = OverrideValue(value)
            mGenerators.put(elementName, generator)
        }

        operator fun get(elementName: Any): Float {
            if (elementName is CLString) {
                val stringValue: String = elementName.content()
                if (mGenerators.containsKey(stringValue)) {
                    return mGenerators.get(stringValue)!!.value()
                }
                if (mMargins.containsKey(stringValue)) {
                    return mMargins.get(stringValue)!!.toFloat()
                }
            } else if (elementName is CLNumber) {
                return elementName.float
            }
            return 0f
        }

        fun getList(elementName: String?): ArrayList<String>? {
            return if (mArrayIds.containsKey(elementName)) {
                mArrayIds.get(elementName)
            } else null
        }

        fun put(elementName: String, elements: ArrayList<String>) {
            mArrayIds[elementName] = elements
        }
    }

    interface GeneratedValue {
        fun value(): Float
    }

    /**
     * Generate a floating point value
     */
    internal class Generator(start: Float, incrementBy: Float) : GeneratedValue {
        var mStart = 0f
        var mIncrementBy = 0f
        var mCurrent = 0f
        var mStop = false

        init {
            mStart = start
            mIncrementBy = incrementBy
            mCurrent = start
        }

        override fun value(): Float {
            if (!mStop) {
                mCurrent += mIncrementBy
            }
            return mCurrent
        }
    }

    /**
     * Generate values like button1, button2 etc.
     */
    internal class FiniteGenerator(
        from: Float,
        to: Float,
        step: Float,
        prefix: String?,
        postfix: String?
    ) : GeneratedValue {
        var mFrom = 0f
        var mTo = 0f
        var mStep = 0f
        var mStop = false
        var mPrefix: String
        var mPostfix: String
        var mCurrent = 0f
        var mInitial: Float
        var mMax: Float

        init {
            mFrom = from
            mTo = to
            mStep = step
            mPrefix = prefix ?: ""
            mPostfix = postfix ?: ""
            mMax = to
            mInitial = from
        }

        override fun value(): Float {
            if (mCurrent >= mMax) {
                mStop = true
            }
            if (!mStop) {
                mCurrent += mStep
            }
            return mCurrent
        }

        fun array(): ArrayList<String> {
            val array: ArrayList<String> = ArrayList<String>()
            var value = mInitial.toInt()
            val maxInt = mMax.toInt()
            for (i in value..maxInt) {
                array.add(mPrefix + value + mPostfix)
                value += mStep.toInt()
            }
            return array
        }
    }

    internal class OverrideValue(var mValue: Float) : GeneratedValue {
        override fun value(): Float {
            return mValue
        }
    }

    //==================== end store variables =========================
    //==================== MotionScene =========================
    enum class MotionLayoutDebugFlags {
        NONE, SHOW_ALL, UNKNOWN
    }
}
