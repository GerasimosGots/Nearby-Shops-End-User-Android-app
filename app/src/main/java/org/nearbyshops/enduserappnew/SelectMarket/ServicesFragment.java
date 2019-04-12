package org.nearbyshops.enduserappnew.SelectMarket;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.google.gson.Gson;

import org.nearbyshops.enduserappnew.DaggerComponentBuilder;
import org.nearbyshops.enduserappnew.Interfaces.NotifySearch;
import org.nearbyshops.enduserappnew.ModelServiceConfig.Endpoints.ServiceConfigurationEndPoint;
import org.nearbyshops.enduserappnew.ModelServiceConfig.ServiceConfigurationGlobal;
import org.nearbyshops.enduserappnew.ModelServiceConfig.ServiceConfigurationLocal;
import org.nearbyshops.enduserappnew.MyApplication;
import org.nearbyshops.enduserappnew.Preferences.PrefGeneral;
import org.nearbyshops.enduserappnew.Preferences.PrefLocation;
import org.nearbyshops.enduserappnew.Preferences.PrefServiceConfig;
import org.nearbyshops.enduserappnew.R;
import org.nearbyshops.enduserappnew.RetrofitRESTContract.ServiceConfigurationService;
import org.nearbyshops.enduserappnew.RetrofitRESTContractSDS.ServiceConfigService;
import org.nearbyshops.enduserappnew.SelectMarket.DeprecatedCode.SlidingLayerSort.UtilitySortServices;
import org.nearbyshops.enduserappnew.Services.UpdateServiceConfiguration;
import org.nearbyshops.enduserappnew.ShopsByCategory.Interfaces.NotifySort;
import org.nearbyshops.enduserappnew.ShopsByCategory.Interfaces.NotifyTitleChanged;
import org.nearbyshops.enduserappnew.Utility.DividerItemDecoration;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;


public class ServicesFragment extends Fragment implements AdapterNew.NoticationsFromServiceAdapter,
        SwipeRefreshLayout.OnRefreshListener , NotifySort, NotifySearch {


//    @Inject
//    OrderServicePFS orderService;

    @Inject Gson gson;

//    @Inject ServiceConfigService serviceConfigService;

    RecyclerView recyclerView;
    AdapterNew adapter;

    public List<ServiceConfigurationGlobal> dataset = new ArrayList<>();

    GridLayoutManager layoutManager;
    SwipeRefreshLayout swipeContainer;


    boolean show = true;

    final private int limit = 5;
    int offset = 0;
    int item_count = 0;
    boolean isDestroyed;



    public ServicesFragment() {

        DaggerComponentBuilder.getInstance()
                .getNetComponent()
                .Inject(this);

    }


    public static ServicesFragment newInstance() {
        ServicesFragment fragment = new ServicesFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        setRetainInstance(true);
        View rootView = inflater.inflate(R.layout.fragment_services, container, false);


        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        swipeContainer = (SwipeRefreshLayout)rootView.findViewById(R.id.swipeContainer);


        if(savedInstanceState==null)
        {
            makeRefreshNetworkCall();
        }


        setupRecyclerView();
        setupSwipeContainer();




        ButterKnife.bind(this,rootView);



        return rootView;
    }





    void setupSwipeContainer()
    {
        if(swipeContainer!=null) {

            swipeContainer.setOnRefreshListener(this);
            swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                    android.R.color.holo_green_light,
                    android.R.color.holo_orange_light,
                    android.R.color.holo_red_light);
        }

    }


    void setupRecyclerView()
    {

        adapter = new AdapterNew(dataset,this,this);

        recyclerView.setAdapter(adapter);



        recyclerView.addItemDecoration(
                new DividerItemDecoration(getActivity(),DividerItemDecoration.VERTICAL_LIST)
        );



        layoutManager = new GridLayoutManager(getActivity(),1);
        recyclerView.setLayoutManager(layoutManager);

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

//        layoutManager.setSpanCount(metrics.widthPixels/400);





        int spanCount = (int) (metrics.widthPixels/(230 * metrics.density));

        if(spanCount==0){
            spanCount = 1;
        }

        layoutManager.setSpanCount(spanCount);


        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//
//                if(dy > 20)
//                {
//
//                    boolean previous = show;
//
//                    show = false ;
//
//                    if(show!=previous)
//                    {
//                        // changed
//                        Log.d("scrolllog","show");
//
//                        if(getActivity() instanceof ToggleFab)
//                        {
//                            ((ToggleFab)getActivity()).hideFab();
//                        }
//                    }
//
//                }else if(dy < -20)
//                {
//
//                    boolean previous = show;
//
//                    show = true;
//
//                    if(show!=previous)
//                    {
//                        Log.d("scrolllog","hide");
//
//                        if(getActivity() instanceof ToggleFab)
//                        {
//                            ((ToggleFab)getActivity()).showFab();
//                        }
//                    }
//                }
//
//            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);


                if(offset + limit > layoutManager.findLastVisibleItemPosition()+1-1)
                {
                    return;
                }


                if(layoutManager.findLastVisibleItemPosition()==dataset.size()-1+1)
                {
                    // trigger fetch next page

//                    if(layoutManager.findLastVisibleItemPosition() == previous_position)
//                    {
//                        return;
//                    }


                    if((offset+limit)<=item_count)
                    {
                        offset = offset + limit;
                        makeNetworkCall(false);
                    }

//                    previous_position = layoutManager.findLastVisibleItemPosition();

                }

            }
        });
    }



//    int previous_position = -1;



    @Override
    public void onRefresh() {

        offset = 0;
        makeNetworkCall(true);
    }


    void makeRefreshNetworkCall()
    {

        swipeContainer.post(new Runnable() {
            @Override
            public void run() {
                swipeContainer.setRefreshing(true);

                onRefresh();
            }
        });

    }


    void makeNetworkCall(final boolean clearDataset)
    {

//            Shop currentShop = UtilityShopHome.getShop(getContext());

        String current_sort = "";
        current_sort = UtilitySortServices.getSort(getContext()) + " " + UtilitySortServices.getAscending(getContext());

//        showToastMessage(UtilityLogin.getAuthorizationHeaders(getActivity()));

        Boolean filterOfficial = null;
        Boolean filterVerified = null;

        if(UtilitySortServices.getOfficial(getActivity()))
        {
            filterOfficial = true;
        }

        if(UtilitySortServices.getVerified(getActivity()))
        {
            filterVerified = true;
        }

        Integer serviceType = null;

        if(UtilitySortServices.getServiceType(getActivity())!=-1)
        {
            serviceType = UtilitySortServices.getServiceType(getActivity());
        }





        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(PrefServiceConfig.getServiceURL_SDS(MyApplication.getAppContext()))
                .client(new OkHttpClient().newBuilder().build())
                .build();





        Call<ServiceConfigurationEndPoint> call = retrofit.create(ServiceConfigService.class).getShopListSimple(
                PrefLocation.getLatitude(getActivity()),
                PrefLocation.getLongitude(getActivity()),
                    null,null,
                    searchQuery,
                    null,null,
                null,
                    current_sort,limit,offset);


//        PrefLocation.getLatitude(getActivity()),
//                PrefLocation.getLongitude(getActivity()),

//        filterOfficial,filterVerified,
//                serviceType,

            call.enqueue(new Callback<ServiceConfigurationEndPoint>() {
                @Override
                public void onResponse(Call<ServiceConfigurationEndPoint> call, Response<ServiceConfigurationEndPoint> response) {

                    if(isDestroyed)
                    {
                        return;
                    }

                    if(response.body()!= null)
                    {
                        item_count = response.body().getItemCount();

                        if(clearDataset)
                        {
                            dataset.clear();
                        }

                        if(response.body().getResults()!=null)
                        {
                            dataset.addAll(response.body().getResults());
                        }
                        adapter.notifyDataSetChanged();
                        notifyTitleChanged();

                    }

                    swipeContainer.setRefreshing(false);

                }

                @Override
                public void onFailure(Call<ServiceConfigurationEndPoint> call, Throwable t) {
                    if(isDestroyed)
                    {
                        return;
                    }

                    showToastMessage("Network Request failed !");
                    swipeContainer.setRefreshing(false);

                }
            });

    }


    @Override
    public void onResume() {
        super.onResume();
        notifyTitleChanged();
        isDestroyed=false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isDestroyed=true;
    }



    void showToastMessage(String message)
    {
        if(getActivity()!=null)
        {
            Toast.makeText(getActivity(),message,Toast.LENGTH_SHORT).show();
        }

    }




    void notifyTitleChanged()
    {

        if(getActivity() instanceof NotifyTitleChanged)
        {
            ((NotifyTitleChanged)getActivity())
                    .NotifyTitleChanged(
                            "Complete (" + String.valueOf(dataset.size())
                                    + "/" + String.valueOf(item_count) + ")",1);


        }
    }







    // Refresh the Confirmed PlaceholderFragment

    private static String makeFragmentName(int viewId, int index) {
        return "android:switcher:" + viewId + ":" + index;
    }




    @Override
    public void notifySortChanged() {
        makeRefreshNetworkCall();
    }




    String searchQuery = null;

    @Override
    public void search(final String searchString) {
        searchQuery = searchString;
        makeRefreshNetworkCall();
    }

    @Override
    public void endSearchMode() {
        searchQuery = null;
        makeRefreshNetworkCall();
    }

    @Override
    public void notifyListItemClick(ServiceConfigurationGlobal serviceConfigurationGlobal) {

        showToastMessage("List item click !");
    }

    @Override
    public void selectMarketClick(ServiceConfigurationGlobal serviceConfigurationGlobal) {




//        getActivity().startService(new Intent(getApplicationContext(), UpdateServiceConfiguration.class));


        if(getActivity() instanceof MarketSelected)
        {
            ((MarketSelected) getActivity()).marketSelected();
//            showToastMessage("Market Selected !");
        }



//        if(PrefServiceConfig.getServiceConfigLocal(this)==null && PrefGeneral.getServiceURL(this)!=null)
//        {
//            // get service configuration when its null ... fetches config at first install or changing service
//            startService(new Intent(getApplicationContext(), UpdateServiceConfiguration.class));
//        }
    }










//    @Override
//    public void notifyCancelOrder(final Order order) {
//
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//
//        builder.setTitle("Confirm Cancel Order !")
//                .setMessage("Are you sure you want to cancel this order !")
//                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                        cancelOrder(order);
//                    }
//                })
//                .setNegativeButton("No", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                        showToastMessage(" Not Cancelled !");
//                    }
//                })
//                .show();
//    }


//    private void cancelOrder(Order order) {
//
//
////        Call<ResponseBody> call = orderService.cancelOrderByShop(order.getOrderID());
//
//        Call<ResponseBody> call = serviceConfigService.cancelledByEndUser(
//                UtilityLogin.getAuthorizationHeaders(getActivity()),
//                order.getOrderID()
//        );
//
//
//        call.enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//
//                if(response.code() == 200 )
//                {
//                    showToastMessage("Successful");
//                    makeRefreshNetworkCall();
//                }
//                else if(response.code() == 304)
//                {
//                    showToastMessage("Not Cancelled !");
//                }
//                else
//                {
//                    showToastMessage("Server Error");
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ResponseBody> call, Throwable t) {
//
//                showToastMessage("Network Request Failed. Check your internet connection !");
//            }
//        });
//
//    }



    public interface MarketSelected
    {
        void marketSelected();
    }






    @OnClick(R.id.fab)
    void fabClick()
    {

//        showToastMessage("Fab clicked !");
        showDialogSubmitURL();
    }





    private void showDialogSubmitURL()
    {
        FragmentManager fm = getChildFragmentManager();
        SubmitURLDialog submitURLDialog = new SubmitURLDialog();
        submitURLDialog.show(fm,"serviceUrl");
    }


}