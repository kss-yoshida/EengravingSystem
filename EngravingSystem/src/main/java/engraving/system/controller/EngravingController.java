package engraving.system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.io.File;
import java.text.*;
import javax.servlet.http.HttpSession;

import engraving.system.entity.*;
import engraving.system.repository.*;

@Controller
@SessionAttributes(value = "user")
public class EngravingController {
	@ModelAttribute(value = "user")
	public User setUpUser() {
		return new User();
	}

	// Repositoryインターフェースを自動インスタンス化
	@Autowired
	private AttendanceRepository attendanceinfo;
	@Autowired
	private ChangeRepository changeinfo;
	@Autowired
	private UserRepository userinfo;
	@Autowired
	private LoginLogRepository logininfo;
	@Autowired
	private RequestRepository requestinfo;
	@Autowired
	HttpSession session;

	/*
	 * 出勤退勤処理
	 */
	// 「/startEngraving」にアクセスがあった場合
	@RequestMapping(value = "/startEngraving", method = RequestMethod.POST)
	public ModelAndView engravingStart(@RequestParam("cmd") String cmd, ModelAndView mav) {

		// 勤怠情報を格納するAttendanceの作成
		User user = (User) session.getAttribute("user");
		Attendance attendance = new Attendance();

		// 日付の受け取りと格納
		Date date = new Date();
		SimpleDateFormat day = new SimpleDateFormat("yyyyMMdd");

		ArrayList<Attendance> attendanceList = (ArrayList<Attendance>) attendanceinfo
				.findByDayAndEmployeeId(day.format(date), user.getEmployeeId());
		if (attendanceList.size() == 0) {
			attendance.setEmployeeId(user.getEmployeeId());
			attendance.setDay(day.format(date));
			// 出勤時間の受け取りと格納
			SimpleDateFormat time = new SimpleDateFormat("kk:mm");
			attendance.setStartTime(time.format(date));
			attendance.setStartEngrave(time.format(date));
			// 情報をDBに保存
			attendanceinfo.saveAndFlush(attendance);
			String startTime = attendance.getStartTime();
			mav.addObject("startTime", startTime);
		} else {
			SimpleDateFormat time = new SimpleDateFormat("kk:mm");
			attendance = attendanceList.get(0);
			Date startEngrave;
			try {
				startEngrave = time.parse(attendance.getStartEngrave());
				String strStartTime = (time.format(startEngrave));
				mav.addObject("startTime", strStartTime);
			} catch (Exception e) {
			}

			Date finishEngrave;
			try {
				finishEngrave = time.parse(attendance.getFinishEngrave());
				String strFinishTime = (time.format(finishEngrave));
				mav.addObject("finishTime", strFinishTime);
			} catch (Exception e) {
			}
		}

		// リダイレクト先を指定
		mav.setViewName(cmd);

		// ModelとView情報を返す
		return mav;
	}

	// 「finishEngraving」にアクセスがあった場合
	@RequestMapping(value = "/finishEngraving", method = RequestMethod.POST)
	public ModelAndView finishEngraving(@RequestParam("cmd") String cmd, ModelAndView mav) {

		// 入力された情報を更新
		Date date = new Date();
		SimpleDateFormat day = new SimpleDateFormat("yyyyMMdd");
		Attendance attendance;
		// ユーザーの当日の打刻情報を呼び出す
		User user = (User) session.getAttribute("user");
		String employeeId = user.getEmployeeId();
		ArrayList<Attendance> attendanceList = (ArrayList<Attendance>) attendanceinfo
				.findByDayAndEmployeeId(day.format(date), employeeId);

		if (attendanceList.get(0).getFinishEngrave() == null && attendanceList.size() != 0) {
			attendance = attendanceList.get(0);

			// 退勤の打刻を行う
			// 退勤時間をattendanceに格納
			SimpleDateFormat time = new SimpleDateFormat("kk:mm");
			attendance.setFinishEngrave(time.format(date));
			attendance.setFinishTime(time.format(date));

			// 勤務時間（時間）の計算
			int hour = Integer.parseInt(attendance.getStartTime().substring(0, 1))
					- Integer.parseInt(attendance.getFinishTime().substring(0, 1));
			// 勤務時間（分）の計算
			int minute = Integer.parseInt(attendance.getStartTime().substring(3, 4))
					- Integer.parseInt(attendance.getFinishTime().substring(3, 4));
			if (minute < 0) {
				minute = 60 + minute;
				hour = hour - 1;
			}
			// 休憩時間・残業時間の計算と格納
			if (hour < 9) {// 8時間未満の勤務の場合
				attendance.setBreakTime("01:00");
				attendance.setOverTime("00:00");
			} else {// ８時間以上の勤務の場合
				attendance.setBreakTime("01:15");
				// 残業時間の計算
				hour = hour - 9;
				minute = minute - 15;
				if (minute < 0) {
					minute = 60 + minute;
				}
				attendance.setOverTime(hour + ":" + minute);
			}
			attendanceinfo.saveAndFlush(attendance);
			// 打刻時間を送る処理
			Date startEngrave;
			try {
				startEngrave = time.parse(attendance.getStartEngrave());
				String strStartTime = (time.format(startEngrave));
				mav.addObject("startTime", strStartTime);
			} catch (Exception e) {
			}

			Date finishEngrave;
			try {
				finishEngrave = time.parse(attendance.getFinishEngrave());
				String strFinishTime = (time.format(finishEngrave));
				mav.addObject("finishTime", strFinishTime);
			} catch (Exception e) {
			}
		} else {// すでに打刻している場合の打刻時間を送る処理
			SimpleDateFormat time = new SimpleDateFormat("kk:mm");
			attendance = attendanceList.get(0);
			Date startEngrave;
			try {
				startEngrave = time.parse(attendance.getStartEngrave());
				String strStartTime = (time.format(startEngrave));
				mav.addObject("startTime", strStartTime);
			} catch (Exception e) {
			}

			Date finishEngrave;
			try {
				finishEngrave = time.parse(attendance.getFinishEngrave());
				String strFinishTime = (time.format(finishEngrave));
				mav.addObject("finishTime", strFinishTime);
			} catch (Exception e) {
			}
		}

		// リダイレクト先を指定
		mav.setViewName(cmd);

		// ModelとView情報を返す
		return mav;
	}

	/*
	 * ログインの処理 セッションの設定とログイン履歴の登録
	 */
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public ModelAndView login(ModelAndView mav, @RequestParam("password") String pass,
			@RequestParam("employee_id") String id) {
//		ユーザーリストの取得
		ArrayList<User> list = (ArrayList<User>) userinfo.findAll();
		User user = new User();
		LoginLog log = new LoginLog();
		String cmd = "";

//		入力した社員番号とパスワードがあるか確認
		for (int i = 0; i < list.size(); i++) {
			user = list.get(i);
			if (user.getEmployeeId().equals(id) && user.getPassword().equals(pass)) {
//				ログイン履歴の設定
				log.setEmployeeId(user.getEmployeeId());
				Date date = new Date();
				SimpleDateFormat format = new SimpleDateFormat("YY-MM-dd HH-mm-ss"); // 年-月-日 時-分-秒
				String datetime = format.format(date);
				log.setLoginTime(datetime);

//				セッションの設定
				session.setAttribute("user", user);
//				ログイン履歴をDBに追加
				logininfo.saveAndFlush(log);
//				該当ユーザーがいたらcmdをOKにする
				cmd = "ok";

				break;

			}
		}

//		ログインした社員の勤怠情報の取得
		Date date = new Date();
		SimpleDateFormat day = new SimpleDateFormat("yyyyMMdd");
		String employeeId = user.getEmployeeId();
		ArrayList<Attendance> attendanceList = (ArrayList<Attendance>) attendanceinfo
				.findByDayAndEmployeeId(day.format(date), employeeId);
		Attendance attendance = new Attendance();
		if (attendanceList.size() != 0) {
//		ログイン日の出勤情報があった場合
			attendance = attendanceList.get(0);
			SimpleDateFormat time = new SimpleDateFormat("kk:mm");
			Date startEngrave;
			Date finishEngrave;
			try {
				startEngrave = time.parse(attendance.getStartEngrave());
				finishEngrave = time.parse(attendance.getFinishEngrave());
				mav.addObject("startTime", time.format(startEngrave));
				mav.addObject("finishTime", time.format(finishEngrave));
			} catch (Exception e) {
			}

		}

		if (cmd.equals("")) {
//			エラーがあったらログインに戻る

		} else {
//			OKになってたらメニューに行く
			mav.addObject("user", user);
			if (user.getAuthority().equals("1")) {

				mav.setViewName("employeeMenu");
			} else {
				mav.setViewName("adminMenu");
			}
		}
		return mav;
	}

	/*
	 * 社員一覧表示 管理者用機能
	 */
	@RequestMapping("/employeeList")
	public ModelAndView employeeList(ModelAndView mav) {
//		DBから社員リストを取得
		ArrayList<User> userList = new ArrayList<User>();
		userList = (ArrayList<User>) userinfo.findAll();
//		リストが空でなければmavに登録
		if (userList.size() != 0) {
			mav.addObject("employeeList", userList);
		}
//		遷移先の指定
		mav.setViewName("employeeList");
		return mav;
	}

	/*
	 * 社員一覧検索機能
	 */
	@RequestMapping("/searchEmployee")
	public ModelAndView search(ModelAndView mav, @RequestParam(value = "employeeId", defaultValue = "0") String id,
			@RequestParam(value = "name", defaultValue = "") String name) {
//		DBから条件付きで社員リストを取得
		ArrayList<User> userList = new ArrayList<User>();
//		社員番号と名前が入力されている
		if (!name.equals("") && id.equals("0")) {
			userList = userinfo.findByEmployeeIdAndName(id, name);
		} else if (!name.equals("")) {
//			名前のみ
			userList = userinfo.findByName(name);
		} else if (!id.equals("0")) {
//			社員番号のみ
			userList = userinfo.findByEmployeeIdStartingWith(id);
		}

//		リストが空でなければmavに登録
		if (userList.size() != 0) {
			mav.addObject("employeeList", userList);
		}
//		遷移先の指定
		mav.setViewName("employeeList");
		return mav;
	}

	/*
	 * 管理者登録削除
	 */
	@RequestMapping("/changeAuthority")
	public String changeAdmin(ModelAndView mav, @RequestParam("authority") String authority,
			@RequestParam("employeeId") String id) {
		User user= userinfo.findByEmployeeId(id);
		if (authority.equals("0")) {
			user.setAuthority("1");
		} else {
			user.setAuthority("0");
		}

		userinfo.saveAndFlush(user);

		return "redirect:/employeeList";
	}

	/*
	 * ログアウト処理
	 * 
	 */
	@RequestMapping("/logout")
	public ModelAndView logout() {
		ModelAndView mav = new ModelAndView();
		session.removeAttribute("user");
		mav.setViewName("login");
		return mav;
	}

	/*
	 * 勤怠情報確認機能
	 */
	@RequestMapping(value = "/attendanceRecord")
	public ModelAndView attendanceRecorde(
			@RequestParam(value = "year", defaultValue = "", required = false) String year,
			@RequestParam(value = "month", defaultValue = "", required = false) String month, ModelAndView mav) {
		// ユーザー情報の受け取り
		User user = (User) session.getAttribute("user");
		String employeeId = user.getEmployeeId();
		String authority = user.getAuthority();

		// 月が1桁で入力されt場合の処理
		if (month.length() == 1 && month != "") {
			month = "0" + month;
		}

		// 検索する日付を格納する変数
		String day;
		Date date = new Date();

		// 渡す年月の形を変換するフォーマット
		SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy年MM月");

		// 検索条件での条件分岐
		if (year.equals("") && month.equals("")) {// 検索条件がない場合
			SimpleDateFormat monthData = new SimpleDateFormat("yyyy-MM");
			day = monthData.format(date) + "%";
			mav.addObject("month", monthFormat.format(date));
		} else if (!year.equals("") && !month.equals("")) {// 年と月で検索された場合
			day = year + "-" + month + "%";
			mav.addObject("month", year + "年" + month + "月");
		} else if (year.equals("") && !month.equals("")) {// 月で検索した場合
			SimpleDateFormat yearData = new SimpleDateFormat("yyyy");
			day = yearData.format(date) + "-" + month + "%";
			mav.addObject("month", yearData.format(date) + "年" + month + "月");
		} else {// 年で検索した場合
			day = year + "%";
			mav.addObject("month", year + "年");
		}
		// ユーザーの勤怠情報の受け取り
		ArrayList<Attendance> attendanceList = attendanceinfo.findByEmployeeIdAndDayLike(employeeId, day);

		// 形の変換のためのフォーマット
		SimpleDateFormat time = new SimpleDateFormat("HH時mm分");
		SimpleDateFormat timer = new SimpleDateFormat("HH時間mm分");
		SimpleDateFormat days = new SimpleDateFormat("dd日");
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");

		// 勤怠情報の形の変換

		for (int i = 0; attendanceList.size() > i; i++) {
			Date startEngrave;
			Date finishEngrave;
			Date startTime;
			Date finishTime;
			Date breakTime;
			Date overTime;
			Date dayData;
			try {
				Attendance attendance = attendanceList.get(i);
				// 出勤打刻時間
				startEngrave = timeFormat.parse(attendance.getStartEngrave());
				attendance.setStartEngrave(time.format(startEngrave));
				// 退勤打刻時間
				finishEngrave = timeFormat.parse(attendance.getFinishEngrave());
				attendance.setFinishEngrave(time.format(finishEngrave));
				// 出勤時間
				startTime = timeFormat.parse(attendance.getStartTime());
				attendance.setStartTime(time.format(startTime));
				// 退勤時間
				finishTime = timeFormat.parse(attendance.getFinishTime());
				attendance.setFinishTime(time.format(finishTime));
				// 休憩時間
				breakTime = timeFormat.parse(attendance.getBreakTime());
				attendance.setBreakTime(timer.format(breakTime));
				// 残業時間
				overTime = timeFormat.parse(attendance.getOverTime());
				attendance.setOverTime(timer.format(overTime));
				// 日付
				dayData = dayFormat.parse(attendance.getDay());
				attendance.setDay(days.format(dayData));

				// データの格納
				attendanceList.set(i, attendance);
			} catch (Exception e) {
			}
		}

		// 情報の受け渡し
		mav.addObject("attendanceList", attendanceList);
		mav.addObject("authority", authority);

		// 管理者が見る場合
		if (authority.equals("0")) {
			mav.addObject("employeeId", user.getEmployeeId());
		}

		// 遷移先の指定
		mav.setViewName("attendanceRecord");

		return mav;
	}

	/*
	 * ユーザー一覧から来た場合の勤怠情報確認機能
	 */
	@RequestMapping(value = "/adminAttendanceRecord")
	public ModelAndView adminAttendanceRecorde(
			@RequestParam(value = "year", defaultValue = "", required = false) String year,
			@RequestParam(value = "month", defaultValue = "", required = false) String month,
			@RequestParam(value = "employeeId", defaultValue = "", required = false) String id, ModelAndView mav) {
		// ユーザー情報の受け取り
		User user = (User) session.getAttribute("user");
		String authority = user.getAuthority();

		// 月が1桁で入力されt場合の処理
		if (month.length() == 1 && month != "") {
			month = "0" + month;
		}

		// 検索する日付を格納する変数
		String day;
		Date date = new Date();

		// 渡す年月の形を変換するフォーマット
		SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy年MM月");

		// 検索条件での条件分岐
		if (year.equals("") && month.equals("")) {// 検索条件がない場合
			SimpleDateFormat monthData = new SimpleDateFormat("yyyy-MM");
			day = monthData.format(date) + "%";
			mav.addObject("month", monthFormat.format(date));
		} else if (!year.equals("") && !month.equals("")) {// 年と月で検索された場合
			day = year + "-" + month + "%";
			mav.addObject("month", year + "年" + month + "月");
		} else if (year.equals("") && !month.equals("")) {// 月で検索した場合
			SimpleDateFormat yearData = new SimpleDateFormat("yyyy");
			day = yearData.format(date) + "-" + month + "%";
			mav.addObject("month", yearData.format(date) + "年" + month + "月");
		} else {// 年で検索した場合
			day = year + "%";
			mav.addObject("month", year + "年");
		}
		// ユーザーの勤怠情報の受け取り
		ArrayList<Attendance> attendanceList = attendanceinfo.findByEmployeeIdAndDayLike(id, day);

		// 形の変換のためのフォーマット
		SimpleDateFormat time = new SimpleDateFormat("HH時mm分");
		SimpleDateFormat timer = new SimpleDateFormat("HH時間mm分");
		SimpleDateFormat days = new SimpleDateFormat("dd日");
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");

		// 勤怠情報の形の変換

		for (int i = 0; attendanceList.size() > i; i++) {
			Date startEngrave;
			Date finishEngrave;
			Date startTime;
			Date finishTime;
			Date breakTime;
			Date overTime;
			Date dayData;
			try {
				Attendance attendance = attendanceList.get(i);
				// 出勤打刻時間
				startEngrave = timeFormat.parse(attendance.getStartEngrave());
				attendance.setStartEngrave(time.format(startEngrave));
				// 退勤打刻時間
				finishEngrave = timeFormat.parse(attendance.getFinishEngrave());
				attendance.setFinishEngrave(time.format(finishEngrave));
				// 出勤時間
				startTime = timeFormat.parse(attendance.getStartTime());
				attendance.setStartTime(time.format(startTime));
				// 退勤時間
				finishTime = timeFormat.parse(attendance.getFinishTime());
				attendance.setFinishTime(time.format(finishTime));
				// 休憩時間
				breakTime = timeFormat.parse(attendance.getBreakTime());
				attendance.setBreakTime(timer.format(breakTime));
				// 残業時間
				overTime = timeFormat.parse(attendance.getOverTime());
				attendance.setOverTime(timer.format(overTime));
				// 日付
				dayData = dayFormat.parse(attendance.getDay());
				attendance.setDay(days.format(dayData));

				// データの格納
				attendanceList.set(i, attendance);
			} catch (Exception e) {
			}
		}

		// 情報の受け渡し
		mav.addObject("attendanceList", attendanceList);
		mav.addObject("authority", authority);
		mav.addObject("employeeId", id);

		// 遷移先の指定
		mav.setViewName("attendanceRecord");

		return mav;
	}

	/*
	 * 社員登録処理
	 */
	@RequestMapping(value = "/employeeRegistration", method = RequestMethod.POST)

	public String employeeRegistrationPost(
			@RequestParam(value = "name", defaultValue = "", required = false) String name,
			@RequestParam(value = "employeeId", defaultValue = "", required = false) String employeeId,
			@RequestParam(value = "password", defaultValue = "", required = false) String password,
			@RequestParam(value = "email", defaultValue = "", required = false) String email,
			@RequestParam(value = "photo", defaultValue = "", required = false) String photo,
			@RequestParam(value = "authority", defaultValue = "", required = false) String authority)
			 {
		
		User user = new User();
		
		user.setName(name);
		user.setEmployeeId(employeeId);
		user.setPassword(password);
		user.setEmail(email);
		user.setPhoto(photo);
		user.setAuthority(authority);
		
		//写真を保存する処理
		//File file = File("/src/main/resources/photo", employeeId + "_photo");
		

//		入力データをDBに保存
		userinfo.saveAndFlush(user);

// 		ModelとView情報を返す
		return "redirect:/employeeList";
	}
	/*
	 * 勤怠情報変更
	 */
	@RequestMapping(value = "/changeAttendance", method = RequestMethod.POST)
	public String changeAttendance(
			@RequestParam(value = "attendanceId", defaultValue = "", required = false) String attendanceId,
			@RequestParam(value = "startTime", defaultValue = "", required = false) String startTime,
			@RequestParam(value = "finishTime", defaultValue = "", required = false) String finishTime,
			@RequestParam(value = "overTime", defaultValue = "", required = false) String overTime,
			@RequestParam(value = "breakTime", defaultValue = "", required = false) String breakTime,
			@RequestParam(value = "startEngrave", defaultValue = "", required = false) String startEngrave,
			@RequestParam(value = "finishEngrave", defaultValue = "", required = false) String finishEngrave,
			@RequestParam(value = "employeeId", defaultValue = "", required = false) String employeeId,
			@RequestParam(value = "day", defaultValue = "", required = false) String day, String mav,
			RedirectAttributes redirectAttributes) {
		User user = (User) session.getAttribute("user");

		Attendance before = attendanceinfo.findByAttendanceId(Integer.parseInt(attendanceId));

		Attendance after = new Attendance();
		after.setAttendanceId(Integer.parseInt(attendanceId));
		after.setEmployeeId(before.getEmployeeId());
		after.setDay(day);
		after.setStartTime(startTime);
		after.setFinishTime(finishTime);
		after.setStartEngrave(startEngrave);
		after.setFinishEngrave(finishEngrave);
		after.setOverTime(overTime);
		after.setBreakTime(breakTime);

		ArrayList<Change> list = new ArrayList<Change>();

		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
		String time = sdf.format(date);

//		それぞれの勤怠情報について変更履歴の登録
//		出勤時間
		if (!before.getStartTime().equals(after.getStartTime())) {
			Change change = new Change();
			change.setAdminId(employeeId);
			change.setIsUpdated(time);
			change.setEmployeeId(before.getEmployeeId());
			change.setDataName("出勤時間");
			change.setBeforeData(before.getStartTime());
			change.setAfterData(after.getStartTime());
			list.add(change);
		}
//		休憩時間
		if (!before.getBreakTime().equals(after.getBreakTime())) {
			Change change = new Change();
			change.setAdminId(employeeId);
			change.setIsUpdated(time);
			change.setEmployeeId(before.getEmployeeId());
			change.setDataName("休憩時間");
			change.setBeforeData(before.getBreakTime());
			change.setAfterData(after.getBreakTime());
			list.add(change);
		}
//		残業時間
		if (!before.getOverTime().equals(after.getOverTime())) {
			Change change = new Change();
			change.setAdminId(employeeId);
			change.setIsUpdated(time);
			change.setEmployeeId(before.getEmployeeId());
			change.setDataName("残業時間");
			change.setBeforeData(before.getOverTime());
			change.setAfterData(after.getOverTime());
			list.add(change);
		}
//		退勤時間
		if (!before.getFinishTime().equals(after.getFinishTime())) {
			Change change = new Change();
			change.setAdminId(employeeId);
			change.setIsUpdated(time);
			change.setEmployeeId(before.getEmployeeId());
			change.setDataName("退勤時間");
			change.setBeforeData(before.getFinishTime());
			change.setAfterData(after.getFinishTime());
			list.add(change);
		}

//		勤怠情報変更の登録
		attendanceinfo.saveAndFlush(after);
//		遷移先コントローラーの選択
		if (user.getEmployeeId().equals(employeeId)) {
			mav = "redirect:/attendanceRecord";
		} else {
//			get送信
			mav = "redirect:/adminAttendanceRecord";
		}

//		変更情報をMapに登録
		ModelMap md = new ModelMap();
		md.addAttribute("changeList", list);
		redirectAttributes.addFlashAttribute("map1", md);
		redirectAttributes.addFlashAttribute("move", mav);

		return "redirect:/changeInsert";
	}

	/*
	* リクエスト情報の受け取り
	*/
	@RequestMapping("/changeRequestList")
	public ModelAndView changeRequestList(ModelAndView mav) {
		//requestInfoの情報をすべて受け取る
		ArrayList<Request> requestList = (ArrayList<Request>)requestinfo.findAll();
		
		//日付を含めたリクエスト情報を格納する変数
		ArrayList<RequestDay> requestDayList = new ArrayList<RequestDay>();
		//日付の形を変更する処理
		SimpleDateFormat dayType = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy年MM月dd日");
		
		for(int i = 0; i < requestList.size();i++) {
			Request request = requestList.get(i);
			if(request.getIsDeleted() == false) {
			RequestDay requestDay = new RequestDay();
			//勤怠情報IDから日付を検索する
			Attendance attendance = attendanceinfo.findByAttendanceId(request.getAttendanceId());
			
			try {
			//日付のフォーマットを変更する
			Date day = dayType.parse(attendance.getDay());
			requestDay.setDay(dayFormat.format(day));
			}catch(Exception e){
				
			}
			
			requestDay.setRequestId(request.getRequestId());
			requestDay.setEmployeeId(request.getEmployeeId());
			requestDay.setAttendanceId(request.getAttendanceId());
			requestDay.setBeforeStartEngrave(attendance.getStartTime());
			requestDay.setBeforeFinishEngrave(attendance.getFinishTime());
			requestDay.setChangeStartEngrave(request.getChangeStartEngrave());
			requestDay.setChangeFinishEngrave(request.getChangeFinishEngrave());
			requestDay.setComment(request.getComment());
			requestDay.setIsDeleted(request.getIsDeleted());
			
			
			requestDayList.add(requestDay);
			}
		}
		
		mav.addObject("requestDayList",requestDayList);
		
		mav.setViewName("changeRequestList");
		
		return mav;
	}
	
	/*
	 * リクエストされた情報を変更するかリクエストを削除する
	 */
	@RequestMapping(value="/changeRequest",  method = RequestMethod.POST)
	public String changeRequest(RedirectAttributes redirectAttributes,@RequestParam(value = "requestId") String strRequestId,
			@RequestParam(value="cmd") String cmd,ModelAndView mav) {
		// ユーザー情報の受け取り
		User user = (User) session.getAttribute("user");
		
		//対象のリクエスト情報を受け取る
		int requestId = Integer.parseInt(strRequestId);
		Request request = requestinfo.findByRequestId(requestId);
		
		//勤怠情報IDから対象の勤怠情報を検索する
		Attendance attendance = attendanceinfo.findByAttendanceId(request.getAttendanceId());
		
		//対象のリクエスト情報を管理する変数の宣言と情報の登録
		RequestDay requestDay = new RequestDay();
		requestDay.setRequestId(request.getRequestId());
		requestDay.setEmployeeId(request.getEmployeeId());
		requestDay.setAttendanceId(request.getAttendanceId());
		requestDay.setBeforeStartEngrave(attendance.getStartTime());
		requestDay.setBeforeFinishEngrave(attendance.getFinishTime());
		requestDay.setChangeStartEngrave(request.getChangeStartEngrave());
		requestDay.setChangeFinishEngrave(request.getChangeFinishEngrave());
		requestDay.setComment(request.getComment());
		requestDay.setIsDeleted(request.getIsDeleted());
		
		//変更履歴を保存する変数の宣言
		ArrayList<Change> changeList = new ArrayList<Change>();
		
		//日付の受け取り
		SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy/MM/dd kk:mm:ss");
		Date date = new Date();
		
		//認可した場合の処理	
		if(cmd.equals("input")) {
			
			//情報を変更するかのif文
			if(request.getChangeStartEngrave() != null && !requestDay.getBeforeStartEngrave().equals(request.getChangeStartEngrave())) {//出勤時間を変更する場合
				attendance.setStartTime(request.getChangeStartEngrave());
				
				//変更履歴を追加
				Change change = new Change();
				change.setAdminId(user.getEmployeeId());
				change.setDataName(attendance.getDay() + "出勤時間");
				change.setBeforeData(requestDay.getBeforeStartEngrave());
				change.setAfterData(requestDay.getChangeStartEngrave());
				change.setEmployeeId(requestDay.getEmployeeId());
				change.setIsUpdated(dayFormat.format(date));
				
				changeList.add(change);
				//changeinfo.saveAndFlush(change);
			}
			if(request.getChangeFinishEngrave() != null && !requestDay.getBeforeFinishEngrave().equals(request.getChangeFinishEngrave())){//退勤時間を変更する場合
				attendance.setFinishTime(request.getChangeFinishEngrave());
				
				//変更履歴を追加
				Change change = new Change();
				change.setAdminId(user.getEmployeeId());
				change.setDataName(attendance.getDay() + "退勤時間");
				change.setBeforeData(requestDay.getBeforeFinishEngrave());
				change.setAfterData(requestDay.getChangeFinishEngrave());
				change.setEmployeeId(requestDay.getEmployeeId());
				change.setIsUpdated(dayFormat.format(date));
				
				changeList.add(change);
				//changeinfo.saveAndFlush(change);
			}
			
		}
		
		//処理を行った変更リクエストを削除する
		request.setIsDeleted(true);
		requestinfo.saveAndFlush(request);
		
		if(cmd.equals("input")) {
			ModelMap modelMap = new ModelMap();
			modelMap.addAttribute("changeList",changeList);
			redirectAttributes.addFlashAttribute("map1", modelMap);
			redirectAttributes.addFlashAttribute("move","changeRequestList");

			return "redirect:/changeInsert";
		}else
			return "changeRequestList";
	}
	
	//変更履歴を登録する
	@RequestMapping("/changeInsert")
	public String changeinsert(RedirectAttributes redirectAttributes,@ModelAttribute("map1") ModelMap map1,
			@ModelAttribute("move") String move) {
		ArrayList<Change> changeList = (ArrayList<Change>)map1.get("changeList");
		changeinfo.saveAndFlush(changeList.get(0));
		changeList.remove(0);
		
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("changeList",changeList);
		redirectAttributes.addFlashAttribute("map1", modelMap);
		redirectAttributes.addFlashAttribute("move",move);
		
		if(changeList.size() == 0) {
			return move;
		}else {
			return "redirect:/changeInsert";
		}
	}
	
		/*
	 * 社員情報を送る処理
	 */
	@RequestMapping(value = "/changeEmployee",method = RequestMethod.POST)
	public ModelAndView changeEmployeeInfo(ModelAndView mav, @RequestParam(value = "employeeId") String empid) {
		
		User user = userinfo.findByEmployeeId(empid);
		
		mav.addObject("user", user);
		mav.setViewName("changeEmployeeInfo");
		return mav;
		
	}
	
	/*
	 * 社員情報変更処理
	 */
	@RequestMapping(value = "/changeEmployeeInfo",method = RequestMethod.POST)
	public String changeEmployeeInfoPost(
			@RequestParam(value = "name", defaultValue = "", required = false) String name,
			@RequestParam(value = "employeeId", defaultValue = "", required = false) String employeeId,
			@RequestParam(value = "password", defaultValue = "", required = false) String password,
			@RequestParam(value = "email", defaultValue = "", required = false) String email,
			@RequestParam(value = "photo", defaultValue = "", required = false) String photo,
			@RequestParam(value = "authority", defaultValue = "", required = false) String authority)
			 {
		
		User user = new User();
		
		user.setName(name);
		user.setEmployeeId((employeeId));
		user.setPassword(password);
		user.setEmail(email);
		user.setPhoto(photo);
		user.setAuthority(authority);
		

//		入力データをDBに保存
		userinfo.saveAndFlush(user);

// 		ModelとView情報を返す
		return "redirect:/employeeList";
	}

	@RequestMapping("/loginForm")
	public ModelAndView loginForm(ModelAndView mav) {
		mav.setViewName("login");

		return mav;
	}

	@RequestMapping("/employeeMenu")
	public ModelAndView employeeMenu(ModelAndView mav) {
		return mav;
	}

	@RequestMapping("/adminMenu")
	public ModelAndView adminMenu(ModelAndView mav) {
		mav.setViewName("adminMenu");
		return mav;
	}

	@RequestMapping("/employeeRegistration")
	public ModelAndView employeeRegistration(ModelAndView mav) {
		mav.setViewName("employeeRegistration");
		return mav;
	}
	
	@RequestMapping("/changeEmployeeInfo")
	public ModelAndView changeEmployeeInfo(ModelAndView mav) {
		mav.setViewName("changeEmployeeInfo");
		return mav;
	}
	
	@RequestMapping("/userHistoricalCheck")
	public ModelAndView userHistoricalCheck(ModelAndView mav) {
		mav.setViewName("userHistoricalCheck");
		return mav;
	}
		@RequestMapping("/changeAttendanceinfo")
	public ModelAndView changeAttendanceinfo(@RequestParam("employeeId") String employeeId,
			@RequestParam("attendanceId") String attendanceId, ModelAndView mav) {
		User user = userinfo.findByEmployeeId(employeeId);
		Attendance attendance = attendanceinfo.findByAttendanceId(Integer.parseInt(attendanceId));
		mav.addObject(user);
		mav.addObject(attendance);
		mav.setViewName("changeAttendance");
		return mav;
	}

}
