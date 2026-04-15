package com.eduplanpro.activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.eduplanpro.database.AppDatabase
import com.eduplanpro.databinding.ActivityAddEditEventBinding
import com.eduplanpro.models.Event
import com.eduplanpro.utils.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class AddEditEventActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddEditEventBinding
    private lateinit var db: AppDatabase
    private var userId: Int = 0
    private var eventId: Int = -1
    private var isEditMode = false
    private var selectedDate: Long = System.currentTimeMillis()
    private var selectedHour: Int = 12
    private var selectedMinute: Int = 0
    private var selectedAmPm: String = "AM"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getIntExtra("user_id", 0)
        eventId = intent.getIntExtra("event_id", -1)
        isEditMode = intent.getBooleanExtra("is_edit", false)

        db = AppDatabase.getDatabase(this)

        setupReminderSpinner()

        if (isEditMode && eventId != -1) {
            loadEventData()
            binding.titleText.text = "Edit Event"
        } else {
            binding.titleText.text = "Add New Event"
        }

        binding.dateButton.setOnClickListener {
            showDatePicker()
        }

        binding.timeButton.setOnClickListener {
            showTimePicker()
        }

        binding.saveButton.setOnClickListener {
            saveEvent()
        }
    }

    private fun setupReminderSpinner() {
        val reminders = arrayOf("No Reminder", "1 Hour Before", "1 Day Before", "2 Days Before")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, reminders)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.reminderSpinner.adapter = adapter
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = selectedDate

        DatePickerDialog(
            this,
            { _, year, month, day ->
                selectedDate = Calendar.getInstance().apply {
                    set(year, month, day, selectedHour, selectedMinute)
                }.timeInMillis
                binding.dateButton.text = "${day}/${month + 1}/${year}"
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = selectedDate

        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            selectedHour = hourOfDay
            selectedMinute = minute
            selectedAmPm = if (hourOfDay >= 12) "PM" else "AM"

            val displayHour = if (hourOfDay > 12) hourOfDay - 12 else if (hourOfDay == 0) 12 else hourOfDay
            binding.timeButton.text = String.format("%02d:%02d %s", displayHour, minute, selectedAmPm)

            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selectedDate
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            selectedDate = calendar.timeInMillis
        }

        TimePickerDialog(
            this,
            timeSetListener,
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        ).show()
    }

    private fun loadEventData() {
        CoroutineScope(Dispatchers.IO).launch {
            val event = db.eventDao().getEventById(eventId)
            withContext(Dispatchers.Main) {
                event?.let {
                    binding.titleInput.setText(it.title)
                    binding.descriptionInput.setText(it.description)
                    selectedDate = it.date

                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = it.date
                    selectedHour = calendar.get(Calendar.HOUR_OF_DAY)
                    selectedMinute = calendar.get(Calendar.MINUTE)
                    selectedAmPm = if (selectedHour >= 12) "PM" else "AM"

                    val displayHour = if (selectedHour > 12) selectedHour - 12 else if (selectedHour == 0) 12 else selectedHour
                    binding.dateButton.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it.date))
                    binding.timeButton.text = String.format("%02d:%02d %s", displayHour, selectedMinute, selectedAmPm)

                    val reminderPosition = when (it.reminderType) {
                        "1_hour" -> 1
                        "1_day" -> 2
                        "2_days" -> 3
                        else -> 0
                    }
                    binding.reminderSpinner.setSelection(reminderPosition)
                }
            }
        }
    }

    private fun saveEvent() {
        val title = binding.titleInput.text.toString().trim()
        val description = binding.descriptionInput.text.toString().trim()

        if (title.isEmpty()) {
            binding.titleInput.error = "Title is required"
            return
        }

        if (description.isEmpty()) {
            binding.descriptionInput.error = "Description is required"
            return
        }

        val reminderPosition = binding.reminderSpinner.selectedItemPosition
        val reminderType = when (reminderPosition) {
            1 -> "1_hour"
            2 -> "1_day"
            3 -> "2_days"
            else -> "none"
        }

        val event = Event(
            id = if (isEditMode) eventId else 0,
            userId = userId,
            title = title,
            description = description,
            date = selectedDate,
            time = binding.timeButton.text.toString(),
            reminderType = reminderType
        )

        CoroutineScope(Dispatchers.IO).launch {
            if (isEditMode) {
                db.eventDao().updateEvent(event)
            } else {
                val newId = db.eventDao().insertEvent(event)
                event.id = newId.toInt()
            }

            // Schedule reminder if reminder is selected
            if (reminderType != "none") {
                val reminderScheduler = ReminderScheduler(this@AddEditEventActivity)
                reminderScheduler.scheduleReminder(event)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddEditEventActivity, "Event saved! Reminder set for ${binding.timeButton.text}", Toast.LENGTH_LONG).show()
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddEditEventActivity, "Event saved!", Toast.LENGTH_SHORT).show()
                }
            }

            withContext(Dispatchers.Main) {
                finish()
            }
        }
    }
}