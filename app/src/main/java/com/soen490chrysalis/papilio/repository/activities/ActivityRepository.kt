package com.soen490chrysalis.papilio.repository.activities

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.soen490chrysalis.papilio.services.network.IUserApiService
import com.soen490chrysalis.papilio.services.network.IActivityApiService
import com.soen490chrysalis.papilio.services.network.requests.ActivitySearchRequest
import com.soen490chrysalis.papilio.services.network.responses.*
import com.soen490chrysalis.papilio.view.dialogs.EventDate
import com.soen490chrysalis.papilio.view.dialogs.EventTime
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class ActivityRepository(
    private var firebaseAuth: FirebaseAuth,
    private val userAPIService: IUserApiService,
    private val activityAPIService: IActivityApiService,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : IActivityRepository {
    private val logTag = ActivityRepository::class.java.simpleName

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun postNewUserActivity(
        activityTitle: String,
        description: String,
        costPerIndividual: Int,
        costPerGroup: Int,
        groupSize: Int,
        pictures: List<Pair<String, InputStream>>,
        activityDate: EventDate,
        startTime: EventTime,
        endTime: EventTime,
        activityAddress: String
    ): Response<Void> {
        return withContext(coroutineDispatcher)
        {
            val calendar: Calendar = Calendar.getInstance()

            // Set the activity start date and time
            calendar.set(
                activityDate.year,
                activityDate.month,
                activityDate.day,
                startTime.hourOfDay,
                startTime.minute
            )

            val outputFmt =
                SimpleDateFormat("yyyy-MM-dd HH:mm:'00.000 +00:00'") // ISO-8601 date format
            val activityStartTime: String = outputFmt.format(calendar.time)

            calendar.set(
                activityDate.year,
                activityDate.month,
                activityDate.day,
                endTime.hourOfDay,
                endTime.minute
            )

            val activityEndTime: String = outputFmt.format(calendar.time)

            Log.d(logTag, "Activity start & end times: $activityStartTime, $activityEndTime")

            val images: MutableList<MultipartBody.Part> = ArrayList()
            for (pair in pictures) {
                val inputStream = pair.second
                val imageFileExtension = pair.first

                val file = File.createTempFile("tempFile", null, null)
                val out: OutputStream = FileOutputStream(file)
                val buf = ByteArray(1024)
                var len: Int
                while (inputStream.read(buf).also { len = it } > 0) {
                    out.write(buf, 0, len)
                }
                out.close()
                inputStream.close()

                val currentImage = MultipartBody.Part.createFormData(
                    "images", // this name must match the name given in the backend
                    file.name,
                    file.asRequestBody("image/$imageFileExtension".toMediaType())
                )

                images.add(currentImage)
            }

            val activityRequestBody: MutableMap<String, Any> = HashMap()
            activityRequestBody["activity[title]"] = activityTitle
            activityRequestBody["activity[description]"] = description
            activityRequestBody["activity[startTime]"] = activityStartTime
            activityRequestBody["activity[endTime]"] = activityEndTime
            activityRequestBody["activity[address]"] = activityAddress
            activityRequestBody["activity[groupSize]"] = groupSize
            activityRequestBody["activity[costPerIndividual]"] = costPerIndividual
            activityRequestBody["activity[costPerGroup]"] = costPerGroup

            Log.d(logTag, "Finished converting images to a Multipart request body")

            val response =
                userAPIService.postNewUserActivity(
                    firebaseAuth.currentUser?.uid,
                    activityRequestBody,
                    images
                )
            Log.d(logTag, "Post new user activity: $response")

            return@withContext response
        }
    }

    override suspend fun getAllActivities(
        page: String,
        size: String
    ): Triple<Boolean, String, ActivityResponse> {
        return withContext(coroutineDispatcher)
        {
            val response = try {
                val result = activityAPIService.getAllActivities(page, size)
                Log.d(logTag, "getAllActivities: $result")
                Triple(result.isSuccessful, result.message(), result.body()!!)
            }
            catch (e: Exception) {
                Log.d(logTag, "activityRepository getAllActivities() exception: $e")
                Triple(false, e.message.toString(), ActivityResponse("0", listOf<ActivityObject>(), "0", "0"))
            }

            return@withContext response
        }
    }

    override suspend fun getActivity(activityId : Number): Triple<Boolean, String, SingleActivityResponse>{
        return withContext(coroutineDispatcher)
        {
            val response = try {
                val result = activityAPIService.getActivity(activityId)
                Log.d(logTag, "getActivity: $result")
                Triple(result.isSuccessful, result.message(), result.body()!!)
            }
            catch (e: Exception) {
                Log.d(logTag, "activityRepository getActivity() exception: $e")
                Triple(false, e.message.toString(), SingleActivityResponse(false, ActivityObject(
                    null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
                )))
            }

            return@withContext response
        }
    }

    override suspend fun searchActivities(
        query:String
    ): Triple<Boolean, String, SearchActivityResponse> {
        return withContext(coroutineDispatcher)
        {
            val response = try {
                val requestBody = ActivitySearchRequest(
                    keyword = query
                )
                val result = activityAPIService.searchActivities(requestBody)
                Log.d(logTag, "searchActivities: $result")
                Triple(result.isSuccessful, result.message(), result.body()!!)
            }
            catch (e: Exception) {
                Log.d(logTag, "activityRepository searchActivities() exception: $e")
                Triple(false, e.message.toString(), SearchActivityResponse("", "0", listOf<ActivityObjectLight>()))
            }
            Log.d(logTag, "Search activities: $response")

            return@withContext response
        }
    }

    override suspend fun open(activityId : Number) : Pair<Int, String>
    {
        return withContext(coroutineDispatcher)
        {
            val response = try {
                val result = activityAPIService.open(activityId = activityId)
                Pair(result.code(), result.message())
            }
            catch (e: Exception) {
                Log.d(logTag, "activityRepository open exception: $e")
                Pair(400, e.message+"")
            }
            Log.d(logTag, "Activity open: $response")

            return@withContext response
        }
    }

    override suspend fun close(activityId : Number) : Pair<Int, String>
    {
        return withContext(coroutineDispatcher)
        {
            val response = try {
                val result = activityAPIService.close(activityId = activityId)
                Pair(result.code(), result.message())
            }
            catch (e: Exception) {
                Log.d(logTag, "activityRepository close exception: $e")
                Pair(400, e.message+"")
            }
            Log.d(logTag, "Activity close: $response")

            return@withContext response
        }
    }
}