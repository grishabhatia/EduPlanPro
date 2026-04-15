package com.eduplanpro.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.eduplanpro.databinding.ItemEventBinding
import com.eduplanpro.models.Event
import java.text.SimpleDateFormat
import java.util.*

class EventAdapter(
    private val onEditClick: (Event) -> Unit,
    private val onDeleteClick: (Event) -> Unit
) : ListAdapter<Event, EventAdapter.EventViewHolder>(EventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EventViewHolder(private val binding: ItemEventBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(event: Event) {
            binding.eventTitle.text = event.title
            binding.eventDescription.text = event.description

            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

            binding.eventDate.text = dateFormat.format(Date(event.date))
            binding.eventTime.text = timeFormat.format(Date(event.date))

            val reminderText = when (event.reminderType) {
                "1_hour" -> "⏰ Reminder: 1 hour before"
                "1_day" -> "📅 Reminder: 1 day before"
                "2_days" -> "📅 Reminder: 2 days before"
                else -> "🔔 No reminder"
            }
            binding.reminderInfo.text = reminderText

            binding.editButton.setOnClickListener { onEditClick(event) }
            binding.deleteButton.setOnClickListener { onDeleteClick(event) }
        }
    }

    class EventDiffCallback : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem == newItem
        }
    }
}