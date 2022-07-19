package engraving.system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.text.*;
import javax.servlet.http.HttpSession;

import engraving.system.entity.*;
import engraving.system.repository.*;

@Controller
public class EngravingController {

	//Repositoryインターフェースを自動インスタンス化
	@Autowired
	private AttendanceRepository attendanceinfo;
	private ChangeRepository changeinfo;
	private UserRepository userinfo;
	private LoginLogRepository logininfo;
	HttpSession session;
	
	/*
	*出勤退勤処理
	*/
	//「/startEngraving」にアクセスがあった場合
	@RequestMapping(value = "/startEngraving", method = RequestMethod.POST)
	public ModelAndView engravingStart(ModelAndView mav) {
		//勤怠情報を格納するAttendanceの作成
		Attendance attendance = new Attendance();
		User user = (User)session.getAttribute("user");
		attendance.setEmployeeId(user.getEmployeeId());
		//日付の受け取りと格納
		Date date = new Date();
		SimpleDateFormat day = new SimpleDateFormat("yyyyMMdd");
		attendance.setDay(day.format(date));
		//出勤時間の受け取りと格納
		SimpleDateFormat time = new SimpleDateFormat("kk:mm");
		attendance.setStartTime(time.format(date));
		attendance.setStartEngrave(time.format(date));
		//情報をDBに保存
		attendanceinfo.saveAndFlush(attendance);
		
		//リダイレクト先を指定
		mav = new ModelAndView("redirect;/menu");
		
		//ModelとView情報を返す
		return mav;
	}
	
	//「finishEngraving」にアクセスがあった場合
	@RequestMapping(value = "/finishEngraving", method = RequestMethod.POST)
	public ModelAndView finishEngraving(ModelAndView mav) {

		//入力された情報を更新
		Date date = new Date();
		SimpleDateFormat day = new SimpleDateFormat("yyyyMMdd");
		//ユーザーの当日の打刻情報を呼び出す
		User user = (User)session.getAttribute("user");
		int employeeId = user.getEmployeeId();
		String strEmployeeId = Integer.toString(employeeId);
		Attendance attendance =  attendanceinfo.findByDayAndEmployeeId(day.format(date), strEmployeeId);
		
		if(attendance.getFinishEngrave() == null) {
			//退勤の打刻を行う
			//退勤時間をattendanceに格納
			SimpleDateFormat time = new SimpleDateFormat("kk:mm");
			attendance.setFinishEngrave(time.format(date));
			attendance.setFinishTime(time.format(date));
			
			//勤務時間（時間）の計算
			int hour = Integer.parseInt(attendance.getStartTime().substring(0,1)) -
					Integer.parseInt(attendance.getFinishTime().substring(0,1));
			//勤務時間（分）の計算
			int minute =  Integer.parseInt(attendance.getStartTime().substring(3,4)) -
					Integer.parseInt(attendance.getFinishTime().substring(3,4));
			if(minute < 0) {
				minute = 60 + minute;
				hour = hour-1;
			}
			//休憩時間・残業時間の計算と格納
			if(hour < 9) {//8時間未満の勤務の場合
				attendance.setBreakTime("01:00");
				attendance.setOverTime("00:00");
			}else {//８時間以上の勤務の場合
				attendance.setBreakTime("01:15");
				//残業時間の計算
				hour = hour - 9;
				minute = minute - 15;
				if(minute < 0) {
					minute = 60 + minute;
				}
				attendance.setOverTime(hour + ":" + minute);
			}
		}
		
		attendanceinfo.saveAndFlush(attendance);
		
		//リダイレクト先を指定
		mav = new ModelAndView("redirect:/menu");
		
		//ModelとView情報を返す
		return mav;
	}
	/*
	 * ログインの処理
	 * セッションの設定とログイン履歴の登録
	 */
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public ModelAndView login(ModelAndView mav, @RequestParam("password") String pass,
			@RequestParam("employee_id") int num) {
//		ユーザーリストの取得
		ArrayList<User> list = (ArrayList<User>)userinfo.findAll();
		User user = new User();
		LoginLog log = new LoginLog();
		String cmd = "";

//		入力した社員番号とパスワードがあるか確認
		for (int i = 0; i < list.size(); i++) {
			user = list.get(i);
			if (user.getEmployeeId() == num && user.getPassword().equals(pass)) {
//				ログイン履歴の設定
				log.setEmployee_id(user.getEmployeeId());
				Date date = new Date();
				SimpleDateFormat format = new SimpleDateFormat("YY-MM-dd HH-mm-ss"); //年-月-日 時-分-秒
				String datetime = format.format(date);
				log.setLogin_time(datetime);
				
//				セッションの設定
				session.setAttribute("user", user);
//				ログイン履歴をDBに追加
				logininfo.saveAndFlush(log);
//				該当ユーザーがいたらcmdをOKにする
				cmd = "ok";
			}
		}
		
		if (cmd.equals("")) {
//			エラーがあったらログインに戻る
			mav = new ModelAndView("redirect;/login");
		} else {
//			OKになってたらメニューに行く
			mav = new ModelAndView("redirect;/menu");
		}
		return mav;

	}

	@RequestMapping("/loginForm")
	public ModelAndView loginForm(ModelAndView mav) {
		mav.setViewName("login");

		return mav;
	}
	
	@RequestMapping("/employeeMenu")
	public ModelAndView employeeMenu(ModelAndView mav) {
		mav.setViewName("employeeMenu");
		return mav;
	}

	@RequestMapping("/adminMenu")
	public ModelAndView adminMenu(ModelAndView mav) {
		mav.setViewName("adminMenu");
		return mav;
	}
	
}
