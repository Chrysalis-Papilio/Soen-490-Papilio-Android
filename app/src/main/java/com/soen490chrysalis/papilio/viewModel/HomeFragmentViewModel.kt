package com.soen490chrysalis.papilio.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soen490chrysalis.papilio.repository.activities.IActivityRepository
import com.soen490chrysalis.papilio.services.network.responses.ActivityObject
import com.soen490chrysalis.papilio.services.network.responses.ActivityResponse
import com.soen490chrysalis.papilio.view.dialogs.EventDate
import kotlinx.coroutines.launch
import java.time.LocalDate

class HomeFragmentViewModel(private val activityRepository: IActivityRepository) : ViewModel() {
    data class FilterOptions(
        val individualCostRange: List<Float>,
        val groupCostRange: List<Float>,
        val startDate: EventDate,
        val endDate: EventDate
    )

    private val logTag = HomeFragmentViewModel::class.java.simpleName
    var activityResponse: MutableLiveData<ActivityResponse> = MutableLiveData<ActivityResponse>()

    val oldestDate = EventDate(2022, 8, 1)
    val furthestDate = EventDate(3022, 8, 1)

    var individualCostLimit: List<Float> = mutableListOf(0f, 1000f)
    var groupCostLimit: List<Float> = mutableListOf(0f, 1000f)
    var filterStartDate: EventDate = oldestDate
    var filterEndDate: EventDate = furthestDate

    fun getAllActivities(page: String, size: String) {
        viewModelScope.launch {
            try {
                val getAllActivitiesResponse = activityRepository.getAllActivities(page, size)
                activityResponse.value = ActivityResponse(
                    getAllActivitiesResponse.third.count,
                    getAllActivitiesResponse.third.rows,
                    getAllActivitiesResponse.third.totalPages,
                    getAllActivitiesResponse.third.currentPage
                )
                Log.d(logTag, "response from getAllAct $getAllActivitiesResponse")
            } catch (e: Exception) {
                Log.d(logTag, "activityRepository.getAllActivities - exception:\n $e")
            }

        }
    }

    fun SetFilter(filters: FilterOptions) {
        individualCostLimit = filters.individualCostRange
        groupCostLimit = filters.groupCostRange
        filterStartDate = filters.startDate
        filterEndDate = filters.endDate
    }

    public fun filterActivity(activity: ActivityObject): Boolean {
        var shouldAdd = true
        val individualCost = activity.costPerIndividual?.toFloat()
        val groupCost = activity.costPerGroup?.toFloat()

        val activityStartTime = activity.startTime
        val dateString = activityStartTime!!.substring(0, activityStartTime.indexOf("T"))
        val dateParts = dateString.split("-")

        val activityDate =
            LocalDate.of(dateParts[0].toInt(), dateParts[1].toInt(), dateParts[2].toInt())
        val startDate =
            LocalDate.of(filterStartDate.year, filterStartDate.month+1, filterStartDate.day)
        val endDate = LocalDate.of(filterEndDate.year, filterEndDate.month+1, filterEndDate.day)

        Log.d("Date -> ", activityDate.toString())

        if (individualCost != null) {
            if (individualCost < individualCostLimit[0] || individualCost > individualCostLimit[1]) {
                shouldAdd = false
            }
        }

        if (groupCost != null && shouldAdd) {
            if (groupCost < groupCostLimit[0] || groupCost > groupCostLimit[1]) {
                shouldAdd = false
            }
        }

        if (shouldAdd) {
            if (activityDate.isBefore(startDate) || activityDate.isAfter(endDate)) {
                shouldAdd = false
            }
        }

        return shouldAdd
    }

    fun ResetFilter() {
        individualCostLimit = mutableListOf(0f, 1000f)
        groupCostLimit = mutableListOf(0f, 1000f)
        filterStartDate = oldestDate
        filterEndDate = furthestDate
    }

    fun GetFilterValues()
    : FilterOptions {
        return FilterOptions(
            individualCostLimit,
            groupCostLimit,
            filterStartDate,
            filterEndDate
        )
    }
}