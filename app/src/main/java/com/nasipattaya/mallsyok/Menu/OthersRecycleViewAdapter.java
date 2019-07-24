package com.nasipattaya.mallsyok.Menu;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nasipattaya.mallsyok.Others.AboutActivity;
import com.nasipattaya.mallsyok.Others.ContactActivity;
import com.nasipattaya.mallsyok.Others.DirectionActivity;
import com.nasipattaya.mallsyok.Others.OpeningActivity;
import com.nasipattaya.mallsyok.Others.ParkingActivity;
import com.nasipattaya.mallsyok.R;

import java.util.List;

public class OthersRecycleViewAdapter extends RecyclerView.Adapter<OthersRecycleViewAdapter.OthersViewHolder>{

    List<OthersActivity.Others> others;
    String TAG = "OthersRecycleViewAdapter";
    Context context;

    OthersRecycleViewAdapter(List<OthersActivity.Others> others, Context context){
        this.others = others;
        this.context = context;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @NonNull
    @Override
    public OthersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_other_menu, parent, false);
        OthersViewHolder othersViewHolder = new OthersViewHolder(v);
        return othersViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull OthersViewHolder holder, int position) {
        final String name = others.get(position).name;

        holder.othersName.setText(name);
        holder.othersImage.setImageResource(others.get(position).image);
        holder.othersCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (name == context.getString(R.string.MENU_OTHERS_CONTACT)){
                    startContactActivity();
                }

                if (name == context.getString(R.string.MENU_OTHERS_OPENING)){
                    startOpeningActivity();
                }

                if (name == context.getString(R.string.MENU_OTHERS_PARKING)){
                    startParkingActivity();
                }

                if (name == context.getString(R.string.MENU_OTHERS_DIRECTION)){
                    startDirectionActivity();
                }

                if (name == context.getString(R.string.MENU_OTHERS_ABOUT_US)){
                    startAboutActivity();
                }
            }
        });
    }

    private void startContactActivity(){
        context.startActivity(new Intent(context, ContactActivity.class));
    }

    private void startParkingActivity(){
        context.startActivity(new Intent(context, ParkingActivity.class));
    }

    private void startDirectionActivity(){
        context.startActivity(new Intent(context, DirectionActivity.class));
    }

    private void startOpeningActivity(){
        context.startActivity(new Intent(context, OpeningActivity.class));
    }

    private void startAboutActivity(){
        context.startActivity(new Intent(context, AboutActivity.class));
    }

    @Override
    public int getItemCount() {
        return others.size();
    }

    public static class OthersViewHolder extends RecyclerView.ViewHolder {
        CardView othersCardView;
        TextView othersName;
        ImageView othersImage;

        OthersViewHolder(View itemView) {
            super(itemView);
            othersCardView = (CardView)itemView.findViewById(R.id.others_card_view);
            othersName = (TextView)itemView.findViewById(R.id.others_name);
            othersImage = (ImageView)itemView.findViewById(R.id.others_image);
        }
    }
}
