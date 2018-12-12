package com.example.reactivearchitecture.nowplaying.adapter

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

import com.example.reactivearchitecture.categories.UnitTest
import com.jakewharton.rxbinding2.support.v7.widget.RecyclerViewScrollEvent
import com.nhaarman.mockitokotlin2.whenever

import org.junit.Test
import org.junit.experimental.categories.Category
import org.mockito.Mockito

import org.assertj.core.api.Assertions.assertThat

@Category(UnitTest::class)
class ScrollEventCalculatorTest {

    @Test
    @Throws(Exception::class)
    fun isAtScrollEnd() {
        //
        // Arrange
        //
        val mockLinearLayoutManager = Mockito.mock(LinearLayoutManager::class.java)
        val mockRecyclerView = Mockito.mock(RecyclerView::class.java)
        val mockRecyclerViewScrollEvent = Mockito.mock(RecyclerViewScrollEvent::class.java)

        whenever(mockLinearLayoutManager.itemCount).thenReturn(50)
        whenever(mockLinearLayoutManager.findLastVisibleItemPosition()).thenReturn(50)

        whenever(mockRecyclerView.layoutManager).thenReturn(mockLinearLayoutManager)

        whenever(mockRecyclerViewScrollEvent.dx()).thenReturn(0)
        whenever(mockRecyclerViewScrollEvent.dy()).thenReturn(100)
        whenever(mockRecyclerViewScrollEvent.view()).thenReturn(mockRecyclerView)

        //
        // Act
        //
        val scrollEventCalculator = ScrollEventCalculator(mockRecyclerViewScrollEvent)
        val value = scrollEventCalculator.isAtScrollEnd

        //
        // Assert
        //
        assertThat(value).isTrue()
    }

    @Test
    @Throws(Exception::class)
    fun isAtScrollEnd_false() {
        //
        // Arrange
        //
        val mockLinearLayoutManager = Mockito.mock(LinearLayoutManager::class.java)
        val mockRecyclerView = Mockito.mock(RecyclerView::class.java)
        val mockRecyclerViewScrollEvent = Mockito.mock(RecyclerViewScrollEvent::class.java)

        whenever(mockLinearLayoutManager.itemCount).thenReturn(50)
        whenever(mockLinearLayoutManager.findLastVisibleItemPosition()).thenReturn(5)

        whenever(mockRecyclerView.layoutManager).thenReturn(mockLinearLayoutManager)

        whenever(mockRecyclerViewScrollEvent.dx()).thenReturn(0)
        whenever(mockRecyclerViewScrollEvent.dy()).thenReturn(100)
        whenever(mockRecyclerViewScrollEvent.view()).thenReturn(mockRecyclerView)

        //
        // Act
        //
        val scrollEventCalculator = ScrollEventCalculator(mockRecyclerViewScrollEvent)
        val value = scrollEventCalculator.isAtScrollEnd

        //
        // Assert
        //
        assertThat(value).isFalse()
    }
}