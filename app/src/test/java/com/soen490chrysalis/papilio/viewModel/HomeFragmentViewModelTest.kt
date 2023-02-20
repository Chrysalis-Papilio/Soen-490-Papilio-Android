package com.soen490chrysalis.papilio.viewModel

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.soen490chrysalis.papilio.repository.mocks.MockActivityRepository
import com.soen490chrysalis.papilio.services.network.responses.ActivityObject
import com.soen490chrysalis.papilio.services.network.responses.ActivityResponse
import com.soen490chrysalis.papilio.testUtils.MainCoroutineRule
import io.mockk.every
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito
import retrofit2.Response

@RunWith(JUnit4::class)
class HomeFragmentViewModelTest
{
    private val mockActivityRepository = MockActivityRepository()
    private val homeFragmentViewModel = HomeFragmentViewModel(mockActivityRepository)

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Before
    fun setUp()
    {
        mockkStatic(Log::class)
        every { Log.v(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAllActivities() = runTest {

        homeFragmentViewModel.getAllActivities("1", "5")

        advanceUntilIdle()

        assert(homeFragmentViewModel.activityResponse.value?.rows?.size == 5)

        homeFragmentViewModel.getAllActivities("1", "10")

        advanceUntilIdle()

        assert(homeFragmentViewModel.activityResponse.value?.rows?.size == 10)

    }



}