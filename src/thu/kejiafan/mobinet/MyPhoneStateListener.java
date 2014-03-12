package thu.kejiafan.mobinet;

import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;

public class MyPhoneStateListener extends PhoneStateListener {
	
	@Override
	public void onCellLocationChanged(CellLocation location) {
		// TODO Auto-generated method stub
		int cid = -1;
		int lac = -1;
		int psc = -1;
		double cellLatitude = -1;
		double cellLongitude = -1;	
		if (location instanceof GsmCellLocation) {
			cid = ((GsmCellLocation) location).getCid() & 0xffff;
			lac = ((GsmCellLocation) location).getLac();
			psc = ((GsmCellLocation) location).getPsc();
		} else if (location instanceof CdmaCellLocation) {
			cid = ((CdmaCellLocation) location).getBaseStationId();
			lac = ((CdmaCellLocation) location).getNetworkId();
			psc = ((CdmaCellLocation) location).getSystemId();
			cellLatitude = ((CdmaCellLocation) location).getBaseStationLatitude();
			cellLongitude = ((CdmaCellLocation) location).getBaseStationLongitude();
			Config.tvCellLocation.setText(cellLatitude + " " + cellLongitude);
		}
		Config.tvCurrentCell.setText("Cid:" +cid + " Lac:" + lac + " Psc:" + psc);
		if (cid == Config.lastcellid) {

		} else {
			Config.lastcellid = cid;
			Config.handoffNumber++;
			Config.tvHandoffNumber.setText("�л�����:"
					+ String.valueOf(Config.handoffNumber) + " ��������:"
					+ String.valueOf(Config.disconnectNumber));
		}
		super.onCellLocationChanged(location);
	}

	@Override
	public void onDataActivity(int direction) {
		// TODO Auto-generated method stub
		super.onDataActivity(direction);
	}

	@Override
	public void onDataConnectionStateChanged(int state, int networkType) {
		// TODO Auto-generated method stub
		switch (state) {
		case TelephonyManager.DATA_DISCONNECTED:		
			Config.dataConnectionState = "Disconnected";
			if (Config.lastConnect) {
				Config.disconnectNumber++;
				Config.lastConnect = false;
				Config.tvHandoffNumber.setText("�л�����:"
						+ String.valueOf(Config.handoffNumber) + " ��������:"
						+ String.valueOf(Config.disconnectNumber));
			}
			break;
		case TelephonyManager.DATA_CONNECTING:
			Config.dataConnectionState = "Connecting";
			break;
		case TelephonyManager.DATA_CONNECTED:
			Config.dataConnectionState = "Connected";
			Config.lastConnect = true;
			break;
		default:
			Config.dataConnectionState = "Unknown";
			break;
		}
		Config.tvDataConnection.setText(Config.dataConnectionState);
		
		SignalUtil.getCurrentnetworkTypeString(networkType);
		Config.tvNetworkType.setText(Config.networkTypeString);
		
		super.onDataConnectionStateChanged(state, networkType);
	}

	@Override
	public void onServiceStateChanged(ServiceState serviceState) {
		// TODO Auto-generated method stub
		super.onServiceStateChanged(serviceState);
	}

	@Override
	public void onSignalStrengthsChanged(SignalStrength signalStrength) {
		// TODO Auto-generated method stub
		super.onSignalStrengthsChanged(signalStrength);
		/**
		 * ��ȡ�ź�ǿ�Ȳ���
		 */
		Config.gsmSignalStrength = signalStrength.getGsmSignalStrength();
		Config.gsmBitErrorRate = signalStrength.getGsmBitErrorRate();
		Config.cdmaDbm = signalStrength.getCdmaDbm();
		Config.cdmaEcio = signalStrength.getCdmaEcio();
		Config.evdoDbm = signalStrength.getEvdoDbm();
		Config.evdoEcio = signalStrength.getEvdoEcio();
		Config.evdoSnr = signalStrength.getEvdoSnr();
		/**
		 * http://www.oschina.net/code/explore/android-4.0.1/telephony/java/android/telephony/SignalStrength.java
		 * 0: GsmSignalStrength(0-31) GsmBitErrorRate(0-7)
		 * 2: CdmaDbm CdmaEcio EvdoDbm EvdoEcio EvdoSnr(0-8)
		 * 7: LteSignalStrength LteRsrp LteRsrq LteRssnr LteCqi ��4G��ȫΪ-1
		 * getGsmLevel getLteLevel getCdmaLevel getEvdoLevel
		 */
		String allSignal = signalStrength.toString();
		try {
			String[] parts = allSignal.split(" ");			
			Config.lteSignalStrength = Integer.parseInt(parts[8]); //asuLTE
			Config.lteRsrp = Integer.parseInt(parts[9]);
			Config.lteRsrq = Integer.parseInt(parts[10]);
			Config.lteRssnr = Integer.parseInt(parts[11]);
			Config.lteCqi = Integer.parseInt(parts[12]);
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		int level = SignalUtil.getCurrentLevel(signalStrength.isGsm());
		switch (Config.netTypeID) {
		case 4:
			Config.signalStrengthString = "1x:" + Config.cdmaDbm + "  3G:"
					+ Config.evdoDbm + "  Level:" + level;
			Config.SignalParameterString = "CDMA:" + Config.cdmaEcio + " EVDO:"
					+ Config.evdoEcio + " SNR:" + Config.evdoSnr;
			break;
		case 5:
			Config.signalStrengthString = "2G:" + Config.gsmSignalStrength
					+ "  4G:" + Config.lteSignalStrength + "  Level:" + level;
			Config.SignalParameterString = "RSRP:" + Config.lteRsrp + " RSRQ:"
					+ Config.lteRsrq + " SNR:" + Config.lteRssnr;
			break;
		default:
			Config.signalStrengthString = Config.gsmSignalStrength + "  Level:" + level;
			Config.SignalParameterString = "������:" + Config.gsmBitErrorRate;			
			break;
		}
		Config.tvSignalStrength.setText(Config.signalStrengthString);
		Config.tvSignalParameter.setText(Config.SignalParameterString);
	}

}