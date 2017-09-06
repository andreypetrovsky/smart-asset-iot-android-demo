package com.bkx.lab.view.activity;

import android.Manifest;
import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.airbnb.lottie.LottieAnimationView;
import com.bkx.lab.presenter.base.BasePresenterActivity;
import com.bkx.lab.presenter.base.NotificationView;
import com.bkx.lab.presenter.base.PresenterFactory;
import com.bkx.lab.presenter.impl.MainPresenter;
import com.bkx.lab.utils.ClientUtils;
import com.bkx.lab.utils.ShakeDetector;
import com.bkx.lab.utils.UIUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.bkx.lab.BuildConfig;
import com.bkx.lab.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.RuntimePermissions;
import timber.log.Timber;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

@RuntimePermissions
public class MainActivity extends BasePresenterActivity<MainPresenter, NotificationView> implements NotificationView {

    private MainPresenter presenter;
    private SensorManager mSensorManager;
    private ShakeDetector sd;
    @BindView(R.id.buttonContainer)
    View buttonContainer;
    @BindView(R.id.animationAccelerometer)
    LottieAnimationView animationAccelerometer;
    @BindView(R.id.animationError)
    LottieAnimationView animationError;
    @BindView(R.id.animationLocation)
    LottieAnimationView animationLocation;
    @BindView(R.id.shake_button)
    View shakeButton;
    @BindView(R.id.photo_button)
    View photoButton;
    @BindView(R.id.progress_bar_view)
    View progressBar;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private static final int TAKE_PHOTO = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        prepareAnimation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                AboutActivity.start(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void prepareAnimation() {
        animationError.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                animationLocation.setVisibility(View.GONE);
                animationError.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                animationError.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                animationError.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
                animationError.setVisibility(View.GONE);
            }
        });
        animationLocation.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                animationError.setVisibility(View.GONE);
                animationAccelerometer.setVisibility(View.GONE);
                animationLocation.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                animationLocation.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                animationLocation.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
                animationLocation.setVisibility(View.GONE);
            }
        });
        animationAccelerometer.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                animationError.setVisibility(View.GONE);
                animationAccelerometer.setVisibility(View.GONE);
                animationAccelerometer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                animationAccelerometer.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                animationAccelerometer.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
                animationAccelerometer.setVisibility(View.GONE);
            }
        });
        animationError.setVisibility(View.GONE);
        animationAccelerometer.setVisibility(View.GONE);
        animationLocation.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sd != null) {
            sd.start(mSensorManager);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sd != null) {
            sd.stop();
        }
    }

    @Override
    protected void setupActivityComponent() {

    }

    @NonNull
    @Override
    protected PresenterFactory<MainPresenter> getPresenterFactory() {
        return MainPresenter::new;
    }

    @Override
    protected void onPresenterPrepared(@NonNull MainPresenter presenter) {
        this.presenter = presenter;
        presenter.prepare();
        sd = new ShakeDetector(presenter);
    }

    @OnClick(R.id.shake_button)
    void onShakeButtonClick() {
        presenter.onShake();
    }

    @Override
    public void onShakeNotificationSent() {
        animationAccelerometer.cancelAnimation();
        playAnimation(animationAccelerometer);
    }

    @Override
    public void onLocationNotificationSent() {
        progressBar.setVisibility(View.INVISIBLE);
        animationLocation.cancelAnimation();
        playAnimation(animationLocation);

    }

    @Override
    public void onError() {
        progressBar.setVisibility(View.INVISIBLE);
        animationError.cancelAnimation();
        playAnimation(animationError);
    }

    @Override
    public void onUnregistered() {
        startActivity(new Intent(this, RegistrationActivity.class));
        finish();
    }

    private void playAnimation(LottieAnimationView animationView) {
        animationView.playAnimation();
        vibrate(500);
    }

    @OnClick(R.id.photo_button)
    void takeAPhotoCheck() {
        MainActivityPermissionsDispatcher.takeAPhotoWithCheck(MainActivity.this);
    }

    @NeedsPermission({Manifest.permission.CAMERA, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION})
    void takeAPhoto() {
        if (isLocationDisabled()) {
            Snackbar snackbar = Snackbar.make(buttonContainer, R.string.location_disabled_message,
                    Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction(getString(android.R.string.ok),
                    v -> snackbar.dismiss());
            snackbar.show();
            onError();
            return;
        }
        getLastLocation();
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(this.getPackageManager()) != null) {
            Observable.just(this)
                    .observeOn(Schedulers.newThread())
                    .map((ctx) -> presenter.createImageFile(this))
                    .subscribe(photoFile -> {
                        Timber.d(Thread.currentThread().getName());
                        if (photoFile != null) {
                            Uri photoURI = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID
                                    + ".provider", presenter.createImageFile(this));
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                            startActivityForResult(takePictureIntent, TAKE_PHOTO);
                        }
                    }, t -> onError());
        }
    }

    public void getLastLocation() {
        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        presenter.onLocationChanged(location);
                    }
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }


    @OnNeverAskAgain({Manifest.permission.CAMERA, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION})
    void showNeverAskAgain() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.permission_dialog_title)
                .setMessage(R.string.permission_dialog_message)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    dialog.dismiss();
                }).setNegativeButton(getString(android.R.string.cancel), (dialog, which) -> dialog.dismiss()).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TAKE_PHOTO && resultCode == RESULT_OK) {
            try {
                if (!ClientUtils.isNetworkConnected(this)) {
                    UIUtils.showInternetConnectionAlertDialog(this);
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                presenter.uploadFile(this);
            } catch (Exception e) {
                e.printStackTrace();
                onPhotoUploadFail("");
            }
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private boolean isLocationDisabled() {
        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled;
        boolean network_enabled;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            return false;
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            return false;
        }

        return !gps_enabled && !network_enabled;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    private void vibrate(long millis) {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            if (v != null) {
                v.vibrate(millis);
            }
        } else {
            VibrationEffect effect =
                    VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE);
            v.vibrate(effect);
        }
    }

    public void onPhotoUploadFail(@Nullable String message) {
        Snackbar snackbar = Snackbar.make(buttonContainer, "Image uploading failed", Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(getString(android.R.string.ok),
                v -> snackbar.dismiss());

        snackbar.show();
    }

    @Override
    public void onBackPressed() {
        presenter.logout();
        super.onBackPressed();
    }
}