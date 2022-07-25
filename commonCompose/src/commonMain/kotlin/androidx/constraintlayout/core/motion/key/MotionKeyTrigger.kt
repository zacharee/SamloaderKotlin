/*
 * Copyright (C) 2021 The Android Open Source Project
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
package androidx.constraintlayout.core.motion.key

import androidx.constraintlayout.core.motion.CustomVariable
import androidx.constraintlayout.core.motion.MotionWidget
import androidx.constraintlayout.core.motion.utils.FloatRect
import androidx.constraintlayout.core.motion.utils.SplineSet
import androidx.constraintlayout.core.motion.utils.TypedValues.TriggerType

class MotionKeyTrigger : MotionKey() {
    private var mCurveFit = -1
    private var mCross: String? = null
    private var mTriggerReceiver: Int = MotionKey.Companion.UNSET
    private var mNegativeCross: String? = null
    private var mPositiveCross: String? = null
    private var mTriggerID: Int = MotionKey.Companion.UNSET
    private var mTriggerCollisionId: Int = MotionKey.Companion.UNSET

    //   TODO private MotionWidget mTriggerCollisionView = null;
    var mTriggerSlack = .1f
    private var mFireCrossReset = true
    private var mFireNegativeReset = true
    private var mFirePositiveReset = true
    private var mFireThreshold = Float.NaN
    private var mFireLastPos = 0f
    private var mPostLayout = false
    var mViewTransitionOnNegativeCross: Int = MotionKey.Companion.UNSET
    var mViewTransitionOnPositiveCross: Int = MotionKey.Companion.UNSET
    var mViewTransitionOnCross: Int = MotionKey.Companion.UNSET
    var mCollisionRect = FloatRect()
    var mTargetRect = FloatRect()

    init {
        mType = KEY_TYPE
        mCustom = HashMap<String, CustomVariable>()
    }

    override fun getAttributeNames(attributes: HashSet<String>) {}
    override fun addValues(splines: HashMap<String, SplineSet>) {}
    override fun getId(name: String?): Int {
        when (name) {
            VIEW_TRANSITION_ON_CROSS -> return TYPE_VIEW_TRANSITION_ON_CROSS
            VIEW_TRANSITION_ON_POSITIVE_CROSS -> return TYPE_VIEW_TRANSITION_ON_POSITIVE_CROSS
            VIEW_TRANSITION_ON_NEGATIVE_CROSS -> return TYPE_VIEW_TRANSITION_ON_NEGATIVE_CROSS
            POST_LAYOUT -> return TYPE_POST_LAYOUT
            TRIGGER_SLACK -> return TYPE_TRIGGER_SLACK
            TRIGGER_COLLISION_VIEW -> return TYPE_TRIGGER_COLLISION_VIEW
            TRIGGER_COLLISION_ID -> return TYPE_TRIGGER_COLLISION_ID
            TRIGGER_ID -> return TYPE_TRIGGER_ID
            POSITIVE_CROSS -> return TYPE_POSITIVE_CROSS
            NEGATIVE_CROSS -> return TYPE_NEGATIVE_CROSS
            TRIGGER_RECEIVER -> return TYPE_TRIGGER_RECEIVER
        }
        return -1
    }

    /**
     * @TODO: add description
     */
    override fun copy(src: MotionKey): MotionKey {
        super.copy(src)
        val k = src as MotionKeyTrigger
        mCurveFit = k.mCurveFit
        mCross = k.mCross
        mTriggerReceiver = k.mTriggerReceiver
        mNegativeCross = k.mNegativeCross
        mPositiveCross = k.mPositiveCross
        mTriggerID = k.mTriggerID
        mTriggerCollisionId = k.mTriggerCollisionId
        // TODO mTriggerCollisionView = k.mTriggerCollisionView;
        mTriggerSlack = k.mTriggerSlack
        mFireCrossReset = k.mFireCrossReset
        mFireNegativeReset = k.mFireNegativeReset
        mFirePositiveReset = k.mFirePositiveReset
        mFireThreshold = k.mFireThreshold
        mFireLastPos = k.mFireLastPos
        mPostLayout = k.mPostLayout
        mCollisionRect = k.mCollisionRect
        mTargetRect = k.mTargetRect
        return this
    }

    /**
     * @TODO: add description
     */
    override fun clone(): MotionKey {
        return MotionKeyTrigger().copy(this)
    }

    private fun fireCustom(str: String, widget: MotionWidget) {
        var str = str
        val callAll = str.length == 1
        if (!callAll) {
            str = str.substring(1).lowercase()
        }
        for (name in mCustom!!.keys) {
            val lowerCase = name.lowercase()
            if (callAll || lowerCase.matches(Regex(str))) {
                val custom = mCustom!![name]
                custom?.applyToWidget(widget)
            }
        }
    }

    /**
     * @TODO: add description
     */
    fun conditionallyFire(position: Float, child: MotionWidget?) {}

    /**
     * @TODO: add description
     */
    override fun setValue(type: Int, value: Int): Boolean {
        when (type) {
            TriggerType.Companion.TYPE_TRIGGER_RECEIVER -> mTriggerReceiver = value
            TriggerType.Companion.TYPE_TRIGGER_ID -> mTriggerID = toInt(value)
            TriggerType.Companion.TYPE_TRIGGER_COLLISION_ID -> mTriggerCollisionId = value
            TriggerType.Companion.TYPE_VIEW_TRANSITION_ON_NEGATIVE_CROSS -> mViewTransitionOnNegativeCross = value
            TriggerType.Companion.TYPE_VIEW_TRANSITION_ON_POSITIVE_CROSS -> mViewTransitionOnPositiveCross = value
            TriggerType.Companion.TYPE_VIEW_TRANSITION_ON_CROSS -> mViewTransitionOnCross = value
            else -> return super.setValue(type, value)
        }
        return true
    }

    /**
     * @TODO: add description
     */
    override fun setValue(type: Int, value: Float): Boolean {
        mTriggerSlack = when (type) {
            TriggerType.Companion.TYPE_TRIGGER_SLACK -> value
            else -> return super.setValue(type, value)
        }
        return true
    }

    /**
     * @TODO: add description
     */
    override fun setValue(type: Int, value: String?): Boolean {
        when (type) {
            TriggerType.Companion.TYPE_CROSS -> mCross = value
            TriggerType.Companion.TYPE_NEGATIVE_CROSS -> mNegativeCross = value
            TriggerType.Companion.TYPE_POSITIVE_CROSS -> mPositiveCross = value
            else -> return super.setValue(type, value)
        }
        return true
    }

    /**
     * @TODO: add description
     */
    override fun setValue(type: Int, value: Boolean): Boolean {
        mPostLayout = when (type) {
            TriggerType.Companion.TYPE_POST_LAYOUT -> value
            else -> return super.setValue(type, value)
        }
        return true
    }

    companion object {
        private const val TAG = "KeyTrigger"
        const val VIEW_TRANSITION_ON_CROSS = "viewTransitionOnCross"
        const val VIEW_TRANSITION_ON_POSITIVE_CROSS = "viewTransitionOnPositiveCross"
        const val VIEW_TRANSITION_ON_NEGATIVE_CROSS = "viewTransitionOnNegativeCross"
        const val POST_LAYOUT = "postLayout"
        const val TRIGGER_SLACK = "triggerSlack"
        const val TRIGGER_COLLISION_VIEW = "triggerCollisionView"
        const val TRIGGER_COLLISION_ID = "triggerCollisionId"
        const val TRIGGER_ID = "triggerID"
        const val POSITIVE_CROSS = "positiveCross"
        const val NEGATIVE_CROSS = "negativeCross"
        const val TRIGGER_RECEIVER = "triggerReceiver"
        const val CROSS = "CROSS"
        const val TYPE_VIEW_TRANSITION_ON_CROSS = 301
        const val TYPE_VIEW_TRANSITION_ON_POSITIVE_CROSS = 302
        const val TYPE_VIEW_TRANSITION_ON_NEGATIVE_CROSS = 303
        const val TYPE_POST_LAYOUT = 304
        const val TYPE_TRIGGER_SLACK = 305
        const val TYPE_TRIGGER_COLLISION_VIEW = 306
        const val TYPE_TRIGGER_COLLISION_ID = 307
        const val TYPE_TRIGGER_ID = 308
        const val TYPE_POSITIVE_CROSS = 309
        const val TYPE_NEGATIVE_CROSS = 310
        const val TYPE_TRIGGER_RECEIVER = 311
        const val TYPE_CROSS = 312
        const val KEY_TYPE = 5
    }
}
