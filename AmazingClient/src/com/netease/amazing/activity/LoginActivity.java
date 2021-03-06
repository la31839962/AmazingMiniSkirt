package com.netease.amazing.activity;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

import com.example.amazing.R;
import com.netease.amazing.sdk.client.AccountRestClient;
import com.netease.amazing.sdk.dto.UserDTO;
import com.netease.amazing.util.UserInfoStore;

public class LoginActivity extends Activity {


	private String loginName;
	private String password;

	private EditText view_loginName;
	private EditText view_password;
	private CheckBox view_rememberMe;
	private Button view_loginSubmit;
	private static final int MENU_EXIT = Menu.FIRST - 1;
	private static final int MENU_ABOUT = Menu.FIRST;
	/** 用来操作SharePreferences的标识 */
	private final String SHARE_LOGIN_TAG = "MAP_SHARE_LOGIN_TAG";

	/** 如果登录成功后,用于保存用户名到c,以便下次不再输入 */
	private String SHARE_LOGIN_loginName = "MAP_LOGIN_loginName";

	private String SHARE_LOGIN_PASSWORD = "MAP_LOGIN_PASSWORD";

	private boolean isNetError;

	private ProgressDialog proDialog;
//	private boolean isFirstLogin = false;
	/** 登录后台通知更新UI线程,主要用于登录失败,通知UI线程更新界面 */
	Handler loginHandler = new Handler() {
		public void handleMessage(Message msg) {
			isNetError = msg.getData().getBoolean("isNetError");
			if (proDialog != null) {
				proDialog.dismiss();
			}
			if (isNetError) {
				Toast.makeText(LoginActivity.this, "登陆失败:\n1.请检查您网络连接.\n2.请联系我们.!",
						Toast.LENGTH_SHORT).show();
			}
			// 用户名和密码错误
			else {
				Toast.makeText(LoginActivity.this, "登陆失败,请输入正确的用户名和密码!",
						Toast.LENGTH_SHORT).show();
				// 清除以前的SharePreferences密码
				clearSharePassword();
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		findViewsById();
		initView(false);
		// 需要去submitListener里面设置URL
		setListener();
	}

	/** 初始化注册View组件 */
	private void findViewsById() {
		view_loginName = (EditText) findViewById(R.id.loginUserNameEdit);
		view_password = (EditText) findViewById(R.id.loginPasswordEdit);
		view_rememberMe = (CheckBox) findViewById(R.id.loginRememberMeCheckBox);
		view_loginSubmit = (Button) findViewById(R.id.loginSubmit);
		//view_loginRegister = (Button) findViewById(R.id.loginRegister);
	}

	/**
	 * 初始化界面
	 * 
	 * @param isRememberMe
	 *            如果当时点击了RememberMe,并且登陆成功过一次,则saveSharePreferences(true,ture)后,则直接进入
	 * */
	private void initView(boolean isRememberMe) {
		SharedPreferences share = getSharedPreferences(SHARE_LOGIN_TAG, 0);
		String loginName = share.getString(SHARE_LOGIN_loginName, "");
		String password = share.getString(SHARE_LOGIN_PASSWORD, "");
		if (!"".equals(loginName)) {
			view_loginName.setText(loginName);
		}
		if (!"".equals(password)) {
			view_password.setText(password);
			view_rememberMe.setChecked(true);
		}
		// 如果密码也保存了,则直接让登陆按钮获取焦点
		if (view_password.getText().toString().length() > 0) {
			 view_loginSubmit.requestFocus();
		}
		share = null;
	}

	private boolean validateLocalLogin(String loginName, String password,
			String validateUrl) {
		// 用于标记登陆状态
		boolean loginState = false;
		try {
			loginState = AccountRestClient.testLogin(UserInfoStore.url, loginName, password);
		} catch (ClientProtocolException e) {
			isNetError = true;
		} catch (IOException e) {
			isNetError = true;
		}
		
		// 登陆成功
		if (loginState) {
			if (isRememberMe()) {
				saveSharePreferences(true, true);
			} else {
				saveSharePreferences(true, false);
			}
		} else {
			// 如果不是网络错误
			if (!isNetError) {
				clearSharePassword();
			}
		}
		if (!view_rememberMe.isChecked()) {
			clearSharePassword();
		}
		return loginState;
	}

	private void saveSharePreferences(boolean saveloginName, boolean savePassword) {
		SharedPreferences share = getSharedPreferences(SHARE_LOGIN_TAG, 0);
		if (saveloginName) {
			Log.d(this.toString(), "saveloginName="
					+ view_loginName.getText().toString());
			share.edit().putString(SHARE_LOGIN_loginName,
					view_loginName.getText().toString()).commit();
		}
		if (savePassword) {
			share.edit().putString(SHARE_LOGIN_PASSWORD,
					view_password.getText().toString()).commit();
		}
		share = null;
	}

	/** 记住我的选项是否勾选 */
	private boolean isRememberMe() {
		if (view_rememberMe.isChecked()) {
			return true;
		}
		return false;
	}

	/** 登录Button Listener */
	private OnClickListener submitListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			proDialog = ProgressDialog.show(LoginActivity.this, "连接中..",
					"连接中..请稍后....", true, true);
			Thread loginThread = new Thread(new LoginFailureHandler());
			loginThread.start();
		}
	};

	// .start();
	// }
	// };

	/** 记住我checkBoxListener */
	private OnCheckedChangeListener rememberMeListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			if (view_rememberMe.isChecked()) {
				Toast.makeText(LoginActivity.this, "如果登录成功,以后账号和密码会自动输入!",
						Toast.LENGTH_SHORT).show();
			}
		}
	};


	/** 设置监听器 */
	private void setListener() {
		view_loginSubmit.setOnClickListener(submitListener);
		view_rememberMe.setOnCheckedChangeListener(rememberMeListener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_EXIT, 0, getResources().getText(R.string.MENU_EXIT));
		menu.add(0, MENU_ABOUT, 0, getResources().getText(R.string.MENU_ABOUT));
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		super.onMenuItemSelected(featureId, item);
		switch (item.getItemId()) {
		case MENU_EXIT:
			finish();
			break;
		case MENU_ABOUT:
			alertAbout();
			break;
		}
		return true;
	}

	/** 弹出关于对话框 */
	private void alertAbout() {
		new AlertDialog.Builder(LoginActivity.this).setTitle(R.string.MENU_ABOUT)
				.setMessage(R.string.aboutInfo).setPositiveButton(
						R.string.ok_label,
						new DialogInterface.OnClickListener() {
							public void onClick(
									DialogInterface dialoginterface, int i) {
							}
						}).show();
	}

	/** 清除密码 */
	private void clearSharePassword() {
		SharedPreferences share = getSharedPreferences(SHARE_LOGIN_TAG, 0);
		share.edit().putString(SHARE_LOGIN_PASSWORD, "").commit();
		share = null;
	}

	class LoginFailureHandler implements Runnable {
		
		@Override
		public void run() {
			loginName = view_loginName.getText().toString();
			password = view_password.getText().toString();
			boolean loginState = validateLocalLogin(loginName, password,
					UserInfoStore.url);

			// 登陆成功
			if (loginState) {
				// 需要传输数据到登陆后的界面,
				UserInfoStore.loginName = loginName;
				UserInfoStore.password = password;
				try {
					UserDTO user = new AccountRestClient(UserInfoStore.url,loginName,password).getUserInfo();
					UserInfoStore.userId = user.getId();
					UserInfoStore.username = user.getName();
					UserInfoStore.imageDir = user.getHeadPic();
					UserInfoStore.backgroundImageDir = user.getFrontCover();
					UserInfoStore.signature = user.getSignature();
					UserInfoStore.userRole = user.getRole();
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
//				if(isFirstLogin) {
//					//转向欢迎界面
//				}
				Intent intent = new Intent();
				intent.setClass(LoginActivity.this, HomeActivity.class);
				startActivity(intent);
				proDialog.dismiss();
			} else {
				// 通过调用handler来通知UI主线程更新UI,
				Message message = new Message();
				Bundle bundle = new Bundle();
				bundle.putBoolean("isNetError", isNetError);
				message.setData(bundle);
				loginHandler.sendMessage(message);
			}
		}

	}
}
