package com.nandkishor.adityabloodcross;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    SharedPreferences sp,token;
    SharedPreferences.Editor ed;
    final int ADD_ADMIN=1,SEARCH=2,LOGOUT=3,CHANGE_PASSWORD=4,ABOUT_US=5,SHARE=6,NOTIFICATION=7,REQUEST_BLOOD=8,MY_REQUEST=9,HISTORY=10;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sp = getSharedPreferences("Log",MODE_PRIVATE);
        ed = sp.edit();


        //Saving the access token for sending push notification
        DatabaseReference d2 = FirebaseDatabase.getInstance().getReference("Users Log"+"/"+sp.getString("User Type","")+"/"+FirebaseAuth.getInstance().getCurrentUser().getUid());
        token = getSharedPreferences("TOKEN",MODE_PRIVATE);
        d2.child("Token").setValue(token.getString("Token",""));

        //Tabs Layout
        SelectionViewPagerAdapter selectViewPagerAdapter = new SelectionViewPagerAdapter(getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(selectViewPagerAdapter);
        TabLayout snacksAndBeverages_tab = findViewById(R.id.tabs);
        snacksAndBeverages_tab.setupWithViewPager(viewPager);



        FloatingActionButton fab = findViewById(R.id.fab);
        if (sp.getString("User Type","").equals("Users"))
            fab.setVisibility(View.GONE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this,AddMemberActivity.class);
                intent.putExtra("User Type","Users");
                intent.putExtra("Label","Add User Details");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer,toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        addMenuItemInNavMenuDrawer();

        View v = navigationView.getHeaderView(0);
        final TextView name = v.findViewById(R.id.name);
        final TextView regNo = v.findViewById(R.id.regNo);
        if (sp.getString("User Type","").equals("Admin")){
            DatabaseReference db = FirebaseDatabase.getInstance().getReference("Users Log"+"/"+"Admin"+"/"+FirebaseAuth.getInstance().getCurrentUser().getUid());
            db.keepSynced(true);
            db.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    name.setText("Welcome "+dataSnapshot.child("Name").getValue());
                    regNo.setText((CharSequence) dataSnapshot.child("Reg or Emp ID").getValue());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
        }else if (sp.getString("User Type","").equals("Users")){
            DatabaseReference db = FirebaseDatabase.getInstance().getReference("Users Log"+"/"+"Users"+"/"+FirebaseAuth.getInstance().getCurrentUser().getUid());
            db.keepSynced(true);
            db.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    name.setText("Welcome "+dataSnapshot.child("Name").getValue());
                    regNo.setText((CharSequence) dataSnapshot.child("Reg or Emp ID").getValue());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
            builder.setCancelable(false);
            builder.setMessage("Do you want to exit?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                    HomeActivity.super.onBackPressed();
                }
            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id){
            case ADD_ADMIN:
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                builder.setTitle("Are you sure ?");
                builder.setMessage("You want to add Admin");
                builder.setCancelable(false);
                builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        Intent intent1 = new Intent(HomeActivity.this,AddMemberActivity.class);
                        intent1.putExtra("User Type","Admin");
                        intent1.putExtra("Label","Add Admin Details");
                        intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent1);
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
            case SEARCH:
                break;
            case LOGOUT:
                if (new CommonAdapter(HomeActivity.this).isNetworkAvailable()){
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(HomeActivity.this);
                    builder1.setCancelable(false);
                    builder1.setMessage("Do you want to logout ?");
                    builder1.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                            FirebaseAuth.getInstance().signOut();
                            Intent intent2 = new Intent(HomeActivity.this,LoginActivity.class);
                            intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            ed.clear().apply();
                            startActivity(intent2);
                            finish();
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
                    AlertDialog dialog1 = builder1.create();
                    dialog1.show();
                }else {
                    RelativeLayout homeLayout = findViewById(R.id.homeLayout);
                    Snackbar.make(homeLayout,"No internet",Snackbar.LENGTH_LONG).show();
                }
                break;
            case CHANGE_PASSWORD:
                Intent intent3 = new Intent(HomeActivity.this,ResetPasswordActivity.class);
                intent3.putExtra("Page","HomePage");
                intent3.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent3);
                break;
            case ABOUT_US:
                //Uri uri = Uri.parse("https://technicalhub.io");
                //Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                Intent intent = new Intent(HomeActivity.this,AboutDeveloper.class);
                startActivity(intent);
                break;
            case SHARE:
                Intent shareIntent;
                shareIntent=new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT,"My APP");
                shareIntent.putExtra(Intent.EXTRA_TEXT,"https://www.youtube.com/watch?v=cYOB941gyXI");
                startActivity(Intent.createChooser(shareIntent,"send via"));
                break;
            case NOTIFICATION:
                Intent intent5 = new Intent(HomeActivity.this,NotificationActivity.class);
                intent5.putExtra("NOTIFICATION","NOTIFICATION");
                intent5.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent5);
                break;
            case REQUEST_BLOOD:
                Intent intent4 = new Intent(HomeActivity.this,RequestBloodActivity.class);
                intent4.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent4);
                break;
            case MY_REQUEST:
                Intent intent6 = new Intent(HomeActivity.this,NotificationActivity.class);
                intent6.putExtra("NOTIFICATION","REQUEST");
                intent6.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent6);
                break;
            case HISTORY:
                Intent intent7 = new Intent(HomeActivity.this,History.class);
                intent7.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent7);
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void addMenuItemInNavMenuDrawer() {
        NavigationView navView = findViewById(R.id.nav_view);

        Menu menu = navView.getMenu();

        if (sp.getString("User Type","").equals("Admin")){
            //Navigation menu list for admin
            menu.add(0,ADD_ADMIN,1,"Add Admin").setIcon(R.drawable.add_member);
            //menu.add(0,SEARCH,2,"Search").setIcon(R.drawable.search);
            menu.add(0,NOTIFICATION,3,"Notifications").setIcon(R.drawable.notifications_icon);
            menu.add(0,HISTORY,4,"My History").setIcon(R.drawable.history);
            menu.add(0,CHANGE_PASSWORD,5,"Change Password").setIcon(R.drawable.change_password);
            menu.add(0,LOGOUT,6,"Logout").setIcon(R.drawable.logout);
            Menu submenu1 = menu.addSubMenu(0,0,7,"Communicate");
            submenu1.add(0,ABOUT_US,1,"About Us").setIcon(R.drawable.about_us);
            submenu1.add(0,SHARE,2,"Share").setIcon(R.drawable.ic_menu_share);
        }
        else
        if (sp.getString("User Type","").equals("Users")){
            //Navigation menu list for Users
            menu.add(0,REQUEST_BLOOD,2,"Request for Blood").setIcon(R.drawable.request);
            menu.add(0,MY_REQUEST,3,"View My Requests").setIcon(R.drawable.view_request);
            menu.add(0,NOTIFICATION,4,"Notifications").setIcon(R.drawable.notifications_icon);
            menu.add(0,HISTORY,5,"My History").setIcon(R.drawable.history);
            menu.add(0,CHANGE_PASSWORD,6,"Change Password").setIcon(R.drawable.change_password);
            menu.add(0,LOGOUT,7,"Logout").setIcon(R.drawable.logout);
            Menu submenu1 = menu.addSubMenu(1,0,7,"Communicate");
            submenu1.add(1,ABOUT_US,1,"About Us").setIcon(R.drawable.about_us);
            submenu1.add(1,SHARE,2,"Share").setIcon(R.drawable.ic_menu_share);
        }
        else {
            menu.add(0,REQUEST_BLOOD,2,"Request for Blood").setIcon(R.drawable.request);
            menu.add(0,MY_REQUEST,3,"View Requests").setIcon(R.drawable.view_request);
            menu.add(0,NOTIFICATION,4,"Notifications").setIcon(R.drawable.notifications_icon);
            menu.add(0,HISTORY,5,"History").setIcon(R.drawable.history);
            menu.add(0,CHANGE_PASSWORD,6,"Change Password").setIcon(R.drawable.change_password);
            menu.add(0,LOGOUT,7,"Logout").setIcon(R.drawable.logout);
            Menu submenu1 = menu.addSubMenu(1,0,8,"Communicate");
            submenu1.add(1,ABOUT_US,1,"About Us").setIcon(R.drawable.about_us);
            submenu1.add(1,SHARE,2,"Share").setIcon(R.drawable.ic_menu_share);
        }

        navView.invalidate();
    }


    class SelectionViewPagerAdapter extends FragmentPagerAdapter {

        SelectionViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return new DonorRepresentationFragment();
                case 1:
                    return new GroupWiseRepresentationFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            String title="";
            switch (position){
                case 0:
                    title = "Blood Donors";
                    break;
                case 1:
                    title = "Blood Groups";
                    break;
            }
            return title;
        }
    }
}
