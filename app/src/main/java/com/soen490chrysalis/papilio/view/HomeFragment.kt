package com.soen490chrysalis.papilio.view

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.slider.RangeSlider
import com.soen490chrysalis.papilio.R
import com.soen490chrysalis.papilio.databinding.ActivityMainBinding
import com.soen490chrysalis.papilio.services.network.responses.ActivityObject
import com.soen490chrysalis.papilio.view.dialogs.EventDate
import com.soen490chrysalis.papilio.view.dialogs.FeedAdapter
import com.soen490chrysalis.papilio.viewModel.HomeFragmentViewModel
import com.soen490chrysalis.papilio.viewModel.factories.HomeFragmentViewModelFactory

class HomeFragment : Fragment()
{
    private val logTag = HomeFragment::class.simpleName
    private lateinit var homeFragmentViewModel : HomeFragmentViewModel
    private lateinit var recyclerView : RecyclerView
    private lateinit var adapter : FeedAdapter
    private var isRecyclerViewInitialized = false
    private var isLoading = false
    private lateinit var progressBar : ProgressBar
    private lateinit var filterButton : ImageButton
    private lateinit var searchButton : ImageButton

    // todo: these variables should be in the view model not in the fragment or activity
    private lateinit var activityList : MutableList<ActivityObject>
    private lateinit var addActivityList : List<ActivityObject>
    private var totalPage : Int = 1
    private var currentPage : Int = 1
    private var pageSize : Int = 5


    override fun onCreateView(
        inflater : LayoutInflater, container : ViewGroup?, savedInstanceState : Bundle?
    ) : View?
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view : View, savedInstanceState : Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        progressBar = view.findViewById(R.id.feed_progress_Bar)
        recyclerView = view.findViewById(R.id.activityFeedRV)
        filterButton = view.findViewById(R.id.filter_button)
        searchButton = view.findViewById(R.id.search_button)


        val homeFragmentVMFactory = HomeFragmentViewModelFactory()
        homeFragmentViewModel =
            ViewModelProvider(this, homeFragmentVMFactory)[HomeFragmentViewModel::class.java]

        // Fetch the first set of activities to display
        homeFragmentViewModel.getAllActivities(currentPage.toString(), pageSize.toString())

        homeFragmentViewModel.activityResponse.observe(viewLifecycleOwner, Observer {
            Log.d(logTag, "RECEIVED NEW DATA: ${it.rows.size}")

            if (isRecyclerViewInitialized)
            {
                val unfilteredList = it.rows
                val finalList = mutableListOf<ActivityObject>()

                for (activity in unfilteredList)
                {
                    if (homeFragmentViewModel.filterActivity(activity))
                    {
                        finalList.add(activity)
                    }
                }

                addActivityList = finalList

                addActivityList.forEach { item ->
                    activityList.add(item)
                }

                Log.d(
                    "NOTIFYING RECYCLER VIEW OF NEWLY INSERTED DATA", activityList.size.toString()
                )

                adapter.notifyDataSetChanged()
                isLoading = false
            }

            if (!isRecyclerViewInitialized)
            {
                // Extract the received data

                val unfilteredList = it.rows
                val finalList = mutableListOf<ActivityObject>()

                for (activity in unfilteredList)
                {
                    if (homeFragmentViewModel.filterActivity(activity))
                    {
                        finalList.add(activity)
                    }
                }

                activityList = finalList // initialize the activityList variable

                // Initialize the recycler view only once
                val layoutManager = LinearLayoutManager(context)
                recyclerView.layoutManager = layoutManager
                recyclerView.itemAnimator = null
                recyclerView.setHasFixedSize(false)
                adapter = FeedAdapter(activityList, this)
                recyclerView.adapter = adapter

                itemClickListener()

                totalPage = it.totalPages.toInt()
                Log.d("TOTAL NBR OF PAGES", totalPage.toString())

                // important to set this variable because we only want to initialize the recycler view once
                isRecyclerViewInitialized = true
            }
        })

        scrollListener()

        filterButton.setOnClickListener {

            val c : Calendar = Calendar.getInstance()
            var startDate = homeFragmentViewModel.oldestDate
            var endDate = homeFragmentViewModel.furthestDate


            val builder = AlertDialog.Builder(activity)
            builder.setTitle("Set Filter Options")

            val inflater = this.layoutInflater
            val dialogLayout = inflater.inflate(R.layout.filters, null)
            val individualCostSlider =
                dialogLayout.findViewById<RangeSlider>(R.id.slider_individual_cost)
            val groupCostSlider = dialogLayout.findViewById<RangeSlider>(R.id.slider_group_cost)
            val startDateButton = dialogLayout.findViewById<TextView>(R.id.select_start_date_btn)
            val endDateButton = dialogLayout.findViewById<TextView>(R.id.select_end_date_btn)
            val ic_start_value_text = dialogLayout.findViewById<TextView>(R.id.ic_start_value)
            val ic_end_value_text = dialogLayout.findViewById<TextView>(R.id.ic_end_value)
            val gc_start_value_text = dialogLayout.findViewById<TextView>(R.id.gc_start_value)
            val gc_end_value_text = dialogLayout.findViewById<TextView>(R.id.gc_end_value)


            val currentFilterValues = homeFragmentViewModel.GetFilterValues()

            var individualCostSliderValues = currentFilterValues.individualCostRange
            var groupCostSliderValues = currentFilterValues.groupCostRange
            val currentStartDate = currentFilterValues.startDate
            val currentEndDate = currentFilterValues.endDate
            startDateButton.text =
                "${currentStartDate.month + 1}/${currentStartDate.day}/${currentStartDate.year}"
            endDateButton.text =
                "${currentEndDate.month + 1}/${currentEndDate.day}/${currentEndDate.year}"
            ic_start_value_text.text = "$${individualCostSliderValues[0]}"
            ic_end_value_text.text = "$${individualCostSliderValues[1]}"
            gc_start_value_text.text = "$${groupCostSliderValues[0]}"
            gc_end_value_text.text = "$${groupCostSliderValues[1]}"


            groupCostSlider.values = groupCostSliderValues
            individualCostSlider.values = individualCostSliderValues

            individualCostSlider.addOnChangeListener { _, _, _ ->
                individualCostSliderValues = individualCostSlider.values
                ic_start_value_text.text = "$${individualCostSlider.values[0]}"
                ic_end_value_text.text = "$${individualCostSlider.values[1]}"
            }

            groupCostSlider.addOnChangeListener { _, _, _ ->
                groupCostSliderValues = groupCostSlider.values
                gc_start_value_text.text = "$${groupCostSlider.values[0]}"
                gc_end_value_text.text = "$${groupCostSlider.values[1]}"
            }

            startDateButton.setOnClickListener {
                val dateDialog = DatePickerDialog(requireContext())
                dateDialog.datePicker.minDate = c.timeInMillis
                dateDialog.show()

                dateDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(View.OnClickListener {
                            startDate = EventDate(
                                dateDialog.datePicker.year,
                                dateDialog.datePicker.month,
                                dateDialog.datePicker.dayOfMonth
                            )
                            startDateButton.text =
                                "${startDate.month + 1}/${startDate.day}/${startDate.year}"
                            dateDialog.dismiss()
                        })
            }

            endDateButton.setOnClickListener {
                val dateDialog = DatePickerDialog(requireContext())
                dateDialog.datePicker.minDate = c.timeInMillis
                dateDialog.show()

                dateDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(View.OnClickListener {
                            endDate = EventDate(
                                dateDialog.datePicker.year,
                                dateDialog.datePicker.month,
                                dateDialog.datePicker.dayOfMonth
                            )
                            endDateButton.text =
                                "${endDate.month + 1}/${endDate.day}/${endDate.year}"
                            dateDialog.dismiss()
                        })
            }

            builder.setView(dialogLayout)

            builder.setPositiveButton(
                "Save",
                DialogInterface.OnClickListener { _ : DialogInterface, _ : Int ->
                    homeFragmentViewModel.SetFilter(
                        HomeFragmentViewModel.FilterOptions(
                            individualCostSliderValues, groupCostSliderValues, startDate, endDate
                        )
                    )
                    clearFeed()

                })

            builder.setNeutralButton("Remove All Filters",
                DialogInterface.OnClickListener { _ : DialogInterface, _ : Int ->
                    homeFragmentViewModel.ResetFilter()
                    clearFeed()
                })

            builder.setNegativeButton(
                "Cancel",
                DialogInterface.OnClickListener { dialog : DialogInterface, _ : Int ->
                    run {
                        // When the "Cancel" button is pressed, simply dismiss the dialog
                        dialog.dismiss()
                    }
                })


            val dialog = builder.create()

            dialog.show()

            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(Color.RED)

        }

        searchButton.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()
                    ?.add(
                        ActivityMainBinding.inflate(layoutInflater).fragmentContainerView.id,
                        BrowseFragment()
                    )?.commit()
        }
    }

    private fun itemClickListener()
    {
        adapter.setOnItemClickListener(object : FeedAdapter.OnItemClickListener
        {
            override fun onItemClick(position : Int)
            {
                val intent = Intent(activity, DisplayActivityInfoActivity::class.java)
                intent.putExtra("id", activityList[position].id)
                intent.putExtra("title", activityList[position].title)
                intent.putExtra("description", activityList[position].description)
                intent.putExtra("contact", activityList[position].business?.email)
                intent.putExtra("user_id", activityList[position].user?.firebase_id)
                intent.putExtra("business_id", activityList[position].business?.businessId)
                intent.putExtra(
                    "individualCost",
                    if (activityList[position].costPerIndividual == "0") "FREE" else ("$" + activityList[position].costPerIndividual + "/person")
                )
                intent.putExtra(
                    "groupCost",
                    if (activityList[position].costPerGroup == "0") "FREE" else ("$" + activityList[position].costPerGroup + "/group")
                )
                intent.putExtra("location", activityList[position].address)
                intent.putExtra("closed", activityList[position].closed)

                if (activityList[position].images != null && activityList[position].images?.isNotEmpty() == true)
                {
                    intent.putExtra("images", true)
                    var x = 0
                    Log.d("Size", activityList[position].images!!.size.toString())
                    for (i in activityList[position].images!!)
                    {
                        intent.putExtra("images$x", i)
                        x++
                    }
                    if (x != 5)
                    {
                        for (e in x until 5)
                        {
                            intent.putExtra("images$e", "")
                            x++
                        }
                    }
                }
                else intent.putExtra("images", false)

                startActivity(intent)
            }
        })
    }

    private fun scrollListener()
    {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener()
        {
            override fun onScrolled(recyclerView : RecyclerView, dx : Int, dy : Int)
            {
                super.onScrolled(recyclerView, dx, dy)
                val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager?
                if (!isLoading)
                {
                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == (activityList.size - 1))
                    {
                        // bottom of list!
                        if (currentPage <= totalPage)
                        {
                            ++currentPage
                            // fetch more activities upon button click
                            homeFragmentViewModel.getAllActivities(
                                currentPage.toString(), pageSize.toString()
                            )
                        }
                        isLoading = true
                        progressBar.visibility = View.VISIBLE
                    }
                }
            }
        })
    }

    private fun clearFeed()
    {
        isRecyclerViewInitialized = false
        val size = activityList.size
        activityList.clear()
        adapter.notifyItemRangeRemoved(0, size)
        currentPage = 1
        homeFragmentViewModel.getAllActivities(currentPage.toString(), pageSize.toString())
    }
}

