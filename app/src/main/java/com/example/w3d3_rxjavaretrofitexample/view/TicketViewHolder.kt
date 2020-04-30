package com.example.w3d3_rxjavaretrofitexample.view

import android.content.Context
import android.text.TextUtils
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.w3d3_rxjavaretrofitexample.network.model.Ticket
import kotlinx.android.synthetic.main.ticket_row.view.*

class TicketViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(context: Context, ticket: Ticket, clickListener: (Ticket) -> Unit) {
        Glide.with(context)
            .load(ticket.airline.logo)
            .apply(RequestOptions.circleCropTransform())
            .into(itemView.logo)

        itemView.airline_name.text = ticket.airline.name
        itemView.departure.text = ticket.departure + " Dep"
        itemView.arrival.text = ticket.arrival + " Dest"
        itemView.duration.text = ticket.flightNumber
        itemView.duration.append(", " + ticket.duration)
        itemView.number_of_stops.text = ticket.numberOfStops.toString() + " Stops"
        if (!TextUtils.isEmpty(ticket.instructions)) {
            itemView.duration.append(", " + ticket.instructions)
        }
        if (ticket.price != null) {
            itemView.price.text = "$" + String.format("%.0f", ticket.price!!.price)
            itemView.number_of_seats.text = ticket.price!!.seats + " Seats"
            itemView.loader.visibility = View.INVISIBLE
        } else {
            itemView.loader.visibility = View.VISIBLE
        }
        itemView.setOnClickListener { clickListener(ticket) }
    }

}