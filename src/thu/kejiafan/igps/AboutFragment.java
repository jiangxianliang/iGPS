package thu.kejiafan.igps;

import thu.kejiafan.igps.R.id;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class AboutFragment extends Fragment {	
	Button btnEsc;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.about, container, false);
		btnEsc = (Button) view.findViewById(id.btnExit);
		btnEsc.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
//				android.os.Process.killProcess(android.os.Process.myPid());
				Toast.makeText(getActivity(), "请点击菜单键或返回键退出", Toast.LENGTH_SHORT).show();
			}
		});
		return view;
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
		Config.statistics.pageviewEnd(this, "AboutFragment");
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		Config.statistics.pageviewStart(this, "AboutFragment");
	}
}
