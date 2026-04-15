package com.eduplanpro.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.eduplanpro.adapters.EventAdapter
import com.eduplanpro.database.AppDatabase
import com.eduplanpro.databinding.ActivityMainBinding
import com.eduplanpro.models.Event
import com.eduplanpro.utils.ReminderScheduler
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var db: AppDatabase
    private lateinit var eventAdapter: EventAdapter
    private var userId: Int = 0
    private var userName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getIntExtra("user_id", 0)
        userName = intent.getStringExtra("user_name") ?: "User"

        db = AppDatabase.getDatabase(this)

        setupRecyclerView()
        loadEvents()

        binding.userName.text = "Hello, $userName!"

        binding.fabAddEvent.setOnClickListener {
            val intent = Intent(this, AddEditEventActivity::class.java)
            intent.putExtra("user_id", userId)
            startActivity(intent)
        }

        binding.profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("user_id", userId)
            startActivity(intent)
        }

        binding.refreshLayout.setOnRefreshListener {
            loadEvents()
            binding.refreshLayout.isRefreshing = false
        }
    }

    private fun setupRecyclerView() {
        eventAdapter = EventAdapter(
            onEditClick = { event ->
                val intent = Intent(this, AddEditEventActivity::class.java)
                intent.putExtra("user_id", userId)
                intent.putExtra("event_id", event.id)
                intent.putExtra("is_edit", true)
                startActivity(intent)
            },
            onDeleteClick = { event ->
                deleteEvent(event)
            }
        )

        binding.eventsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.eventsRecyclerView.adapter = eventAdapter
    }

    private fun loadEvents() {
        lifecycleScope.launch {
            db.eventDao().getEventsByUser(userId).collect { events ->
                if (events.isEmpty()) {
                    binding.emptyStateText.visibility = android.view.View.VISIBLE
                    binding.eventsRecyclerView.visibility = android.view.View.GONE
                } else {
                    binding.emptyStateText.visibility = android.view.View.GONE
                    binding.eventsRecyclerView.visibility = android.view.View.VISIBLE
                    eventAdapter.submitList(events)
                }
            }
        }
    }

    private fun deleteEvent(event: Event) {
        lifecycleScope.launch {
            db.eventDao().deleteEvent(event)
            ReminderScheduler(this@MainActivity).cancelReminder(event.id)
            Toast.makeText(this@MainActivity, "Event deleted", Toast.LENGTH_SHORT).show()
            loadEvents()
        }
    }

    override fun onResume() {
        super.onResume()
        loadEvents()
    }
}