package com.soen490chrysalis.papilio.view

import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.mapbox.maps.*
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createCircleAnnotationManager
import com.mapbox.search.*
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.soen490chrysalis.papilio.R

class DisplayActivityInfoActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_acitivity_info)

        // Create Action Bar val so we can 1) display it with a proper title and 2) put a working back button on it
        val actionBar = supportActionBar

        // if Action Bar is not null, then put a back button on it as well as put the "User Profile" title on it
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.title = "Activity Info"
        }


        val infoTile: TextView = findViewById(R.id.info_Title)
        val infoDescription: TextView = findViewById(R.id.info_Description)
        val infoIndividualCost: TextView = findViewById(R.id.individualCost)
        val infoGroupCost: TextView = findViewById(R.id.groupCost)
        val infoAddress: TextView = findViewById(R.id.info_Location)
        val infoImages0: ImageView = findViewById(R.id.info_imageView0)
        val infoImages1: ImageView = findViewById(R.id.info_imageView1)
        val infoImages2: ImageView = findViewById(R.id.info_imageView2)
        val infoImages3: ImageView = findViewById(R.id.info_imageView3)
        val infoImages4: ImageView = findViewById(R.id.info_imageView4)
        val mapView: MapView = findViewById(R.id.mapView)

        val bundle: Bundle? = intent.extras
        val title = bundle!!.getString("title")
        val description = bundle.getString("description")
        val individualCost = bundle.getString("individualCost")
        val groupCost = bundle.getString("groupCost")
        val location = bundle.getString("location")
        val hasImages = bundle.getBoolean("images")
        if (hasImages) {
            val image0 = bundle.getString("images0")
            val image1 = bundle.getString("images1")
            val image2 = bundle.getString("images2")
            val image3 = bundle.getString("images3")
            val image4 = bundle.getString("images4")

            if (image0 != "") {
                Glide.with(this).load(image0).into(infoImages0)
            }
            if (image1 != "") {
                infoImages1.isVisible = true
                Glide.with(this).load(image1).into(infoImages1)

            }
            if (image2 != "") {
                infoImages2.isVisible = true
                Glide.with(this).load(image2).into(infoImages2)

            }
            if (image3 != "") {
                infoImages3.isVisible = true
                Glide.with(this).load(image3).into(infoImages3)
            }
            if (image4 != "") {
                infoImages4.isVisible = true
                Glide.with(this).load(image4).into(infoImages4)
            }

        }

        infoTile.text = title
        infoDescription.text = description
        infoIndividualCost.text = individualCost
        infoGroupCost.text = groupCost
        infoAddress.text = location

        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS)

        // Create a Search Engine object so we can do Forward Geocoding
        val searchEngine = SearchEngine.createSearchEngine(
            SearchEngineSettings(getString(R.string.MAPBOX_PUBLIC_ACCESS_TOKEN))
        )

        // Callback to deal with the results of the select() method in searchEngine.This is where the MapView is created and tuned to be displayed on the activity info page
        val selectCallback = object : SearchSelectionCallback {
            override fun onResult(
                suggestion: SearchSuggestion, result: SearchResult, responseInfo: ResponseInfo
            ) {

                // The activity's location in (latitude, longitude) format
                val coordinates = result.coordinate

                // Setting up bounds for the map camera
                // In other words, making sure that we can't pan away from the activity's location. This ensures that the map is always centered at the activity location
                val cameraBoundsOptions = CameraBoundsOptions.Builder().bounds(
                    CoordinateBounds(
                        coordinates, coordinates, false
                    )
                ).minZoom(4.0).build()

                // Actually creating the map camera and assigning it to our MapView element in the activity display info layout.
                mapView.getMapboxMap().setCamera(
                    CameraOptions.Builder().center(coordinates).zoom(15.0).build()
                )

                // Assigning the camera bounds we created above to the camera we created just above.
                mapView.getMapboxMap().setBounds(cameraBoundsOptions)

                // Display a circular annotation on the map to accurately show on the map where the location of the activity is.
                val annotationApi = mapView.annotations
                val circleAnnotationManager = annotationApi.createCircleAnnotationManager()

                val circleAnnotationOptions: CircleAnnotationOptions =
                    CircleAnnotationOptions().withPoint(coordinates).withCircleRadius(8.0)
                        .withCircleColor("#ee4e8b").withCircleStrokeWidth(2.0)
                        .withCircleStrokeColor("#ffffff")
                circleAnnotationManager.create(circleAnnotationOptions)

            }

            override fun onSuggestions(
                suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo
            ) {
                // Empty for now, maybe stuff will be added here later.
            }

            override fun onCategoryResult(
                suggestion: SearchSuggestion,
                results: List<SearchResult>,
                responseInfo: ResponseInfo
            ) {
                // Empty for now, maybe stuff will be added here later.
            }

            override fun onError(e: Exception) {
                // Empty for now, maybe stuff will be added here later.
            }
        }

        // Callback to handle the Suggestion object when it is fetched by the searchEngine.search() method
        val searchCallback = object : SearchSuggestionsCallback {
            override fun onSuggestions(
                suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo
            ) {
                val suggestion = suggestions.firstOrNull()

                // Using the Suggestion object of our location that we just received, call the select() method in searchEngine
                // to convert our location string into (latitude, longitude) coordinates.
                suggestion?.let { searchEngine.select(it, selectCallback) }
            }

            override fun onError(e: Exception) {
            }
        }

        // Take the activity's location string and fetch a Suggestion object from the MapBox Search API
        val task = location?.let {
            searchEngine.search(
                it, SearchOptions(limit = 1), // making sure we only get 1 suggestion,
                searchCallback
            )
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}