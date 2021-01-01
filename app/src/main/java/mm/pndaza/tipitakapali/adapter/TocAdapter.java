package mm.pndaza.tipitakapali.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import mm.pndaza.tipitakapali.R;
import mm.pndaza.tipitakapali.model.Toc;
import mm.pndaza.tipitakapali.utils.MDetect;
import mm.pndaza.tipitakapali.utils.Rabbit;

public class TocAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TOC_CHAPTER = 0;
    private static final int TOC_TITLE = 1;
    private static final int TOC_SUBHEAD = 2;
    private static final int TOC_SUBSUBHEAD = 3;

    private OnItemClickListener onItemClickListener;
    private ArrayList<Toc> tocs;

    public TocAdapter(ArrayList<Toc> tocs, OnItemClickListener onItemClickListener) {
        this.tocs = tocs;
        this.onItemClickListener = onItemClickListener;

    }

    @Override
    public int getItemViewType(int position) {

        String tocType = tocs.get(position).getType();
        if (tocType.equals("chapter")) {
            return TOC_CHAPTER;
        } else if (tocType.equals("title")) {
            return TOC_TITLE;
        } else if (tocType.equals("subhead")) {
            return TOC_SUBHEAD;
        } else if (tocType.equals("subsubhead")) {
            return TOC_SUBSUBHEAD;
        }
        //default
        return TOC_CHAPTER;
    }


    @Override
    public int getItemCount() {
        return tocs.size();
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case TOC_CHAPTER:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.toc_chapter, parent, false);
                return new ViewHolderChapter(view);
            case TOC_TITLE:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.toc_title, parent, false);
                return new ViewHolderTitle(view);
            case TOC_SUBHEAD:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.toc_subhead, parent, false);
                return new ViewHolderSubhead(view);
            case TOC_SUBSUBHEAD:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.toc_subsubhead, parent, false);
                return new ViewHolderSubsubhead(view);
        }
        throw new IllegalArgumentException();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof ViewHolderChapter) {
            ((ViewHolderChapter) holder).tv_name.setText(Rabbit.uni2zg(tocs.get(position).getName()));
        } else if (holder instanceof ViewHolderTitle) {
            ((ViewHolderTitle) holder).tv_name.setText(Rabbit.uni2zg(tocs.get(position).getName()));
        } else if (holder instanceof ViewHolderSubhead) {
            ((ViewHolderSubhead) holder).tv_name.setText(Rabbit.uni2zg(tocs.get(position).getName()));
        } else {
            ((ViewHolderSubsubhead) holder).tv_name.setText(Rabbit.uni2zg(tocs.get(position).getName()));
        }

    }

    class ViewHolderChapter extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tv_name;

        public ViewHolderChapter(@NonNull View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_name.setTypeface( tv_name.getTypeface(), Typeface.BOLD);
            tv_name.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onItemClickListener.onItemClick(tocs.get(getAdapterPosition()));
        }
    }

    class ViewHolderTitle extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tv_name;

        public ViewHolderTitle(@NonNull View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_name.setTypeface( tv_name.getTypeface(), Typeface.BOLD);
            tv_name.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onItemClickListener.onItemClick(tocs.get(getAdapterPosition()));
        }
    }

    class ViewHolderSubhead extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tv_name;

        public ViewHolderSubhead(@NonNull View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_name.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onItemClickListener.onItemClick(tocs.get(getAdapterPosition()));
        }
    }

    class ViewHolderSubsubhead extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tv_name;

        public ViewHolderSubsubhead(@NonNull View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_name.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onItemClickListener.onItemClick(tocs.get(getAdapterPosition()));
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Toc toc);
    }

    /*Context context;
    ArrayList<String> tocList;
    private static final int TOC_CHAPTER = 0;
    private static final int TOC_TITLE = 1;
    private static final int TOC_SUBHEAD = 2;
    private static final int TOC_SUBSUBHEAD = 3;

    public TocAdapter(Context context, ArrayList<String> tocList) {
        this.context = context;
        this.tocList = tocList;
    }

    @Override
    public int getItemViewType(int position) {
        String toc = tocList.get(position);
        if (toc.startsWith("chapter")) {
            return TOC_CHAPTER;
        } else if (toc.startsWith("title")) {
            return TOC_TITLE;
        } else if (toc.startsWith("subhead")) {
            return TOC_SUBHEAD;
        } else if (toc.startsWith("subsubhead")){
            return TOC_SUBSUBHEAD;
        }
        //default
        return TOC_CHAPTER;
    }

    @Override
    public int getViewTypeCount() {
        return 4;
    }

    @Override
    public int getCount() {
        return tocList.size();
    }

    @Override
    public Object getItem(int i) {
        return tocList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // inflate the layout for each list row
        if (convertView == null) {
            switch (getItemViewType(position)) {
                case TOC_CHAPTER:
                    convertView = LayoutInflater.from(context).
                            inflate(R.layout.toc_chapter, parent, false);
                    break;
                case TOC_TITLE:
                    convertView = LayoutInflater.from(context).
                            inflate(R.layout.toc_title, parent, false);
                    break;
                case TOC_SUBHEAD:
                    convertView = LayoutInflater.from(context).
                            inflate(R.layout.toc_subhead, parent, false);
                    break;
                case TOC_SUBSUBHEAD:
                    convertView = LayoutInflater.from(context).
                            inflate(R.layout.toc_subsubhead, parent, false);
                    break;
            }
        }


        String rawToc = tocList.get(position);
        String[] toc = rawToc.split("->");
        String tocType = toc[0];
        String tocName = toc[1];

        if (tocType.equals("chapter")) {
            TextView tv_chapter = convertView.findViewById(R.id.tv_toc_chapter);
            // display as zawgyi
            tv_chapter.setText(Rabbit.uni2zg(tocName));
            tv_chapter.setTypeface( tv_chapter.getTypeface(), Typeface.BOLD);
        } else if (tocType.equals("title")) {
            TextView tv_title = convertView.findViewById(R.id.tv_toc_title);
            // display as zawgyi
            tv_title.setText(Rabbit.uni2zg(tocName));
            tv_title.setTypeface(tv_title.getTypeface(), Typeface.BOLD);
        } else if (tocType.equals("subhead")) {
            TextView tv_subhead = convertView.findViewById(R.id.tv_toc_subhead);
            // display as zawgyi
            tv_subhead.setText(Rabbit.uni2zg(tocName));
        } else if (tocType.equals("subsubhead")){
            TextView tv_subsubhead = convertView.findViewById(R.id.tv_toc_subsubhead);
            // display as zawgyi
            tv_subsubhead.setText(Rabbit.uni2zg(tocName));
        }
        // returns the view for the current row
        return convertView;
    }*/
}
