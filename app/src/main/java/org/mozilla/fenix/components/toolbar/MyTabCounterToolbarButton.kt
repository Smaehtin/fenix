package org.mozilla.fenix.components.toolbar

import android.view.View
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import mozilla.components.browser.state.selector.getNormalOrPrivateTabs
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.toolbar.Toolbar
import mozilla.components.lib.state.ext.flowScoped
import mozilla.components.support.ktx.android.content.res.resolveAttribute
import mozilla.components.support.ktx.kotlinx.coroutines.flow.ifChanged
import mozilla.components.ui.tabcounter.TabCounter
import mozilla.components.ui.tabcounter.TabCounterMenu
import org.mozilla.fenix.R
import java.lang.ref.WeakReference

/**
 * A [Toolbar.Action] implementation that shows a [TabCounter].
 */
@Suppress("LongParameterList")
open class MyTabCounterToolbarButton(
    private val lifecycleOwner: LifecycleOwner,
    private val countBasedOnSelectedTabType: Boolean = true,
    private val showTabs: () -> Unit,
    private val openNewTab: () -> Unit,
    private val closeTab: () -> Unit,
    private val undoCloseTab: () -> Unit,
    private val store: BrowserStore,
) : Toolbar.Action {

    private var reference = WeakReference<TabCounter>(null)

    override fun createView(parent: ViewGroup): View {
        store.flowScoped(lifecycleOwner) { flow ->
            flow.map { state -> getTabCount(state) }
                .ifChanged()
                .collect { tabs ->
                    updateCount(tabs)
                }
        }

        val tabCounter = TabCounter(parent.context).apply {
            reference = WeakReference(this)

            setOnTouchListener(
                object : OnSwipeTouchListener(context) {
                    override fun onLongPress() {
                        openNewTab()
                    }

                    override fun onSwipeTop() {
                        closeTab()
                    }

                    override fun onSwipeBottom() {
                        undoCloseTab()
                    }

                    override fun onClick() {
                        showTabs.invoke()
                    }
                },
            )

            addOnAttachStateChangeListener(
                object : View.OnAttachStateChangeListener {
                    override fun onViewAttachedToWindow(v: View) {
                        setCount(getTabCount(store.state))
                    }

                    override fun onViewDetachedFromWindow(v: View) { /* no-op */
                    }
                },
            )

            contentDescription =
                parent.context.getString(R.string.mozac_feature_tabs_toolbar_tabs_button)
        }

        // Set selectableItemBackgroundBorderless
        tabCounter.setBackgroundResource(
            parent.context.theme.resolveAttribute(
                android.R.attr.selectableItemBackgroundBorderless,
            ),
        )

        return tabCounter
    }

    override fun bind(view: View) = Unit

    private fun getTabCount(state: BrowserState): Int {
        return if (countBasedOnSelectedTabType) {
            state.getNormalOrPrivateTabs(isPrivate(store)).size
        } else {
            state.tabs.size
        }
    }

    /**
     * Update the tab counter button on the toolbar.
     *
     * @property count the updated tab count
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun updateCount(count: Int) {
        reference.get()?.setCountWithAnimation(count)
    }

    /**
     * Check if the selected tab is private.
     *
     * @property store the [BrowserStore] associated with this instance
     */
    fun isPrivate(store: BrowserStore): Boolean {
        return store.state.selectedTab?.content?.private ?: false
    }
}
