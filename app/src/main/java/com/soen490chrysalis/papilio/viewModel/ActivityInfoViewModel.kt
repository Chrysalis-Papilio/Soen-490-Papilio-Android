package com.soen490chrysalis.papilio.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soen490chrysalis.papilio.repository.activities.IActivityRepository
import com.soen490chrysalis.papilio.repository.users.CheckActivityMember
import com.soen490chrysalis.papilio.repository.users.IUserRepository
import com.soen490chrysalis.papilio.services.network.responses.CheckFavoriteResponse
import com.soen490chrysalis.papilio.services.network.responses.FavoriteActivitiesResponse
import com.soen490chrysalis.papilio.services.network.responses.FavoriteResponse
import kotlinx.coroutines.launch

data class APIResponse(var isSuccess : Boolean, var errorMessage : String)

class ActivityInfoViewModel(
    private val userRepository : IUserRepository,
    private val activityRepository : IActivityRepository
) : ViewModel()
{
    private val logTag = ActivityInfoViewModel::class.java.simpleName
    var checkActivityMemberResponse = MutableLiveData<CheckActivityMember>()
    var joinActivityResponse = MutableLiveData<APIResponse>()
    var leaveActivityResponse = MutableLiveData<APIResponse>()
    var activitiesResponse : MutableLiveData<FavoriteActivitiesResponse> =
        MutableLiveData<FavoriteActivitiesResponse>()
    var checkActivityFavoritedResponse : MutableLiveData<CheckFavoriteResponse> =
        MutableLiveData<CheckFavoriteResponse>()
    var activityFavoritedResponse : MutableLiveData<FavoriteResponse> =
        MutableLiveData<FavoriteResponse>()

    var activityEntryResponse : MutableLiveData<Pair<Int, String>> =
        MutableLiveData<Pair<Int, String>>()

    fun getUserId() : String?
    {
        return userRepository.getUser()?.uid
    }

    // Function that fetches information about whether or not a user has joined an activity
    fun checkActivityMember(activity_id : String)
    {
        viewModelScope.launch {
            val result = userRepository.checkActivityMember(activity_id)
            Log.d(logTag, "response from checkActivityMember(): $result")
            checkActivityMemberResponse.value = result
        }
    }

    fun joinActivity(activity_id : String)
    {
        viewModelScope.launch {
            val result = userRepository.addUserToActivity(activity_id)
            Log.d(logTag, "response from joinActivity(): $result")
            joinActivityResponse.value = APIResponse(result.first, result.second)
        }
    }

    fun leaveActivity(activity_id : String)
    {
        viewModelScope.launch {
            val result = userRepository.removeUserFromActivity(activity_id)
            Log.d(logTag, "response from leaveActivity(): $result")
            leaveActivityResponse.value = APIResponse(result.first, result.second)
        }
    }

    fun checkActivityFavorited(activityId : Number)
    {
        viewModelScope.launch {
            try
            {
                val getActivityResponse = userRepository.isActivityFavorited(activityId.toString())
                checkActivityFavoritedResponse.value = getActivityResponse.third
                Log.d(logTag, "response from isActivityFavorited --> $getActivityResponse")
            }
            catch (e : Exception)
            {
                Log.d(logTag, "userRepository.checkActivityFavorited - exception:\n $e")
            }
        }
    }

    fun addFavoriteActivity(activityId : Number)
    {
        viewModelScope.launch {
            try
            {
                val getActivityResponse = userRepository.addFavoriteActivity(activityId)

                Log.d("addFavoriteActivity", getActivityResponse.second)

                activityFavoritedResponse.value = FavoriteResponse(
                    getActivityResponse.third.success,
                    getActivityResponse.third.update
                )
            }
            catch (e : Exception)
            {
                Log.d(logTag, "userRepository.addFavoriteActivity - exception:\n $e")
            }
        }
    }

    fun removeFavoriteActivity(activityId : Number)
    {
        viewModelScope.launch {
            try
            {
                val getActivityResponse = userRepository.removeFavoriteActivity(activityId)

                Log.d("removeFavoriteActivity", getActivityResponse.second)

                activityFavoritedResponse.value = FavoriteResponse(
                    getActivityResponse.third.success,
                    getActivityResponse.third.update
                )
            }
            catch (e : Exception)
            {
                Log.d(logTag, "userRepository.removeFavoriteActivity - exception:\n $e")
            }
        }
    }

    fun SetActivityEntry(activityId : Number, closed : Boolean)
    {
        viewModelScope.launch {
            if (closed)
            {
                val routeResult = activityRepository.open(activityId)
                activityEntryResponse.value = Pair(routeResult.first, routeResult.second)
            }
            else
            {
                val routeResult = activityRepository.close(activityId)
                activityEntryResponse.value = Pair(routeResult.first, routeResult.second)
            }
        }

    }
}