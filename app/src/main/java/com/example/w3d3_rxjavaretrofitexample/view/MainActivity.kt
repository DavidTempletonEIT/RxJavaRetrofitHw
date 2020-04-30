package com.example.w3d3_rxjavaretrofitexample.view

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.w3d3_rxjavaretrofitexample.R
import com.example.w3d3_rxjavaretrofitexample.network.ApiClient.client
import com.example.w3d3_rxjavaretrofitexample.network.ApiService
import com.example.w3d3_rxjavaretrofitexample.network.model.Ticket
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observables.ConnectableObservable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private val from = "DEL"
    private val to = "HYD"

    // CompositeDisposable is used to dispose the subscriptions in onDestroy() method.
    private val disposable = CompositeDisposable()
    lateinit var apiService: ApiService
    lateinit var mAdapter: TicketsAdapter
    private var ticketsList: MutableList<Ticket> = mutableListOf()
    lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "$from > $to"

        apiService = client!!.create(ApiService::class.java)

        initRecyclerView()

        /***
         * You can notice replay() operator (getTickets(from, to).replay()) is used to make an
         * Observable emits the data on new subscriptions without re-executing the logic again.
         * In our case, the list of tickets will be emitted without making the HTTP call again.
         * Without the replay method, you can notice the fetch tickets HTTP call get executed
         * multiple times.
         */
        val ticketObservable: ConnectableObservable<MutableList<Ticket>> =
            getTickets().replay()

        /**
         * Fetching all tickets first
         * Observable emits List<Ticket> at once
         * All the items will be added to RecyclerView.
         * In the first subscription, the list of tickets directly added to Adapter class and the
         * RecyclerView is rendered directly without price and number of seats.
        </Ticket> */
        disposable.add(
            ticketObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { ticketsList ->
                    mAdapter.updateTickets(ticketsList)
                }.subscribe()
        )
        // Calling connect to start emission
        disposable.add(
            ticketObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                /**

                 * Converting List<Ticket> emission to single Ticket emissions

                </Ticket> */

                .flatMap { tickets ->

                    Observable.fromIterable(tickets)

                }
                /**

                 * Fetching price on each Ticket emission

                 */
                .flatMap { ticket ->
                    /***

                     * If you observe getPriceObservable(), the API call fetches Price model.

                     * But the map() operator is used to convert the return type from Price to Ticket.

                     */
                    getPriceObservable(ticket)
                }
                .subscribeWith(object : DisposableObserver<Ticket?>() {
                    override fun onNext(ticket: Ticket) {
                        // Once the price and seats information is received, the particular row item is updated in RecyclerView.
                        val position = ticketsList.indexOf(ticket)
                        if (position == -1) { // TODO - take action
                            // Ticket not found in the list
                            // This shouldn't happen
                            return }
                        mAdapter.updatePrice(ticket, position) }
                    override fun onError(e: Throwable) {
                        showError() }
                    override fun onComplete() {} })
        )
        ticketObservable.connect()
    }

    private fun initRecyclerView() {
        mAdapter = TicketsAdapter(applicationContext, ticketsList){ ticket : Ticket -> onTicketSelected(ticket) }

        val mLayoutManager: RecyclerView.LayoutManager = GridLayoutManager(this, 1)
        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = mLayoutManager
        recyclerView.addItemDecoration(GridSpacingItemDecoration(1, dpToPx(5), true))
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = mAdapter
    }

    /**
     * Making Retrofit call to fetch all tickets
     */
    private fun getTickets(): Observable<MutableList<Ticket>> {
        return apiService.tickets("DEL","CHE")
        //return apiService.priceSeats("6E-ARI","DEL","CHE")

            .toObservable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
    private fun getPriceObservable(ticket: Ticket): Observable<Ticket> {

        return apiService

            .priceSeats(ticket.flightNumber, ticket.from, ticket.to)

            .toObservable()

            .subscribeOn(Schedulers.io())

            .observeOn(AndroidSchedulers.mainThread())

            .map { price ->
                ticket.price = price
                ticket
            }
    }
    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }

    private fun dpToPx(dp: Int): Int {
        val r: Resources = resources
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            r.displayMetrics
        ).roundToInt()
    }
    private fun onTicketSelected(ticket: Ticket?) {

        Toast.makeText(this, "Clicked: ${ticket?.flightNumber}", Toast.LENGTH_LONG).show()

    }
    private fun showError() {

        Log.e("", "showError: ")

    }
}