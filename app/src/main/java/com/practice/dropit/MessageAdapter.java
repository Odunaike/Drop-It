package com.practice.dropit;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/* since we ave two layouts (based on sending or receiving)  to use for our recycler view.
I will create three containers for the status , sent,and receive.
The status will store if the app is being viewed from the current user account. In tat case other users message will appear on receive
Also we will make use of viewtype parameter in the onCreatewHolder method
 */

public class MessageAdapter extends  RecyclerView.Adapter<MessageAdapter.MyViewHolder> {

    List<ModelClass> list;
    String userName;

    boolean status;
    int send;
    int receive;

    public MessageAdapter(List<ModelClass> list, String userName) {
        this.list = list;
        this.userName = userName;

       // status = false;
        send = 1;
        receive = 2;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        //so I will compare the viewtype to kow the layout to use
        if (viewType == send) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_sent_design, parent, false);
        }else{
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_received_design, parent, false);
        }

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.textView.setText(list.get(position).getMessage());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    //this methos returns 0 by default, asuming there is just one single viewtype.
    //so we compare the "String userName" to the  list member's "String from;" property. if it is = to the userName then this user is sending.
    @Override
    public int getItemViewType(int position) {

        if (list.get(position).getFrom().equals(userName)) {
            status = true;
            return send;
        }else{
            status = false;
            return receive;
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder{

        TextView textView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            if (status) {
                textView = itemView.findViewById(R.id.textViewSentMessage);
            }else{
                textView = itemView.findViewById(R.id.textViewReceivedMessage);
            }
        }
    }

}
