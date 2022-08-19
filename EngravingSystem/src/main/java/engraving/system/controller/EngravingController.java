@RequestMapping("/employeeMenu")
	public ModelAndView employeeMenu(ModelAndView mav,
			@RequestParam(value = "message", defaultValue = "") String message) {
		try {
			// セッション情報の確認
			User user = (User) session.getAttribute("user");
			if (user == null) {
				mav.addObject("error", "セッションが切れました。再度ログインしてください。");
				mav.setViewName("login");
				return mav;
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
					if (attendance.getStartEngrave() != null) {
						startEngrave = time.parse(attendance.getStartEngrave());
						mav.addObject("startTime", time.format(startEngrave));
					}
					if (attendance.getFinishEngrave() != null) {
						finishEngrave = time.parse(attendance.getFinishEngrave());
						mav.addObject("finishTime", time.format(finishEngrave));
					}
				} catch (ParseException e) {
				}
			}
			mav.setViewName("employeeMenu");
			mav.addObject("message", message);
			return mav;
		} catch (Exception e) {
			mav.setViewName("redirect:/logout");
			mav.addObject("error", "DB接続エラーです。DBを確認してください。");
			return mav;
		}
	}package engraving.system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.io.*;
import java.text.*;
import java.awt.image.*;

import javax.imageio.*;
import javax.imageio.stream.*;
import javax.imageio.plugins.jpeg.*;
import javax.servlet.http.HttpSession;

import engraving.system.entity.*;
import engraving.system.repository.*;

@Controller

public class EngravingController {

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

		try {
			//セッション情報の確認
			User user = (User) session.getAttribute("user");
			if (user == null) {
				mav.addObject("error","セッションが切れました。再度ログインしてください。");
				mav.setViewName("login");
				return mav;
			}
			String error = "";
			// 勤怠情報を格納するAttendanceの作成
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

//			すでに打刻時間があった場合
			} else {
				error = "すでに本日の出勤打刻は完了しています。";
				mav.addObject("error", error);
				SimpleDateFormat time = new SimpleDateFormat("kk:mm");
				attendance = attendanceList.get(0);
				Date startEngrave;
				try {
					startEngrave = time.parse(attendance.getStartEngrave());
					String strStartTime = (time.format(startEngrave));
					mav.addObject("startTime", strStartTime);
				} catch (ParseException e) {
				}

				Date finishEngrave;
				try {
					finishEngrave = time.parse(attendance.getFinishEngrave());
					String strFinishTime = (time.format(finishEngrave));
					mav.addObject("finishTime", strFinishTime);
				} catch (ParseException e) {
				}
			}

			// リダイレクト先を指定
			mav.setViewName(cmd);

			// ModelとView情報を返す
			return mav;
		} catch (Exception e) {
			mav.setViewName("redirect:/logout");
			mav.addObject("error", "DB接続エラーです。DBを確認してください。");
			return mav;
		}
	}

	// 「finishEngraving」にアクセスがあった場合
	@RequestMapping(value = "/finishEngraving", method = RequestMethod.POST)
	public ModelAndView finishEngraving(@RequestParam("cmd") String cmd, ModelAndView mav) {
		try {
			//セッション情報の確認
			User user = (User) session.getAttribute("user");
			if (user == null) {
				mav.addObject("error","セッションが切れました。再度ログインしてください。");
				mav.setViewName("login");
				return mav;
			}
			// 入力された情報を更新
			Date date = new Date();
			SimpleDateFormat day = new SimpleDateFormat("yyyyMMdd");
			Attendance attendance;
			// ユーザーの当日の打刻情報を呼び出す
			String error;
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
				int hour = Integer.parseInt(attendance.getFinishTime().substring(0, 2))
						- Integer.parseInt(attendance.getStartTime().substring(0, 2));
				// 勤務時間（分）の計算
				int minute = Integer.parseInt(attendance.getFinishTime().substring(3, 5))
						- Integer.parseInt(attendance.getStartTime().substring(3, 5));
				if (minute < 0) {
					minute = 60 + minute;
					hour = hour - 1;
				}
				// 休憩時間・残業時間の計算と格納
				if (hour < 9) {// 8時間未満の勤務の場合 
					attendance.setBreakTime("01:00");
					attendance.setOverTime("00:00");
				} else {// ８時間以上の勤務の場合
					if(hour == 9 && minute <= 15) {//勤務時間が9時間で時間が15分以内の場合
						attendance.setBreakTime("01:" + minute);
						attendance.setOverTime("00:00");
					}else {
						attendance.setBreakTime("01:15");
						// 残業時間の計算
						hour = hour - 9;
						minute = minute - 15;
						if (minute < 0) {
							minute = 60 + minute;
							hour = hour - 1;
						}
						attendance.setOverTime(hour + ":" + minute);
					}
				}
				attendanceinfo.saveAndFlush(attendance);
				// 打刻時間を送る処理
				Date startEngrave;
				try {
					startEngrave = time.parse(attendance.getStartEngrave());
					String strStartTime = (time.format(startEngrave));
					mav.addObject("startTime", strStartTime);
				} catch (ParseException e) {
				}

				Date finishEngrave;
				try {
					finishEngrave = time.parse(attendance.getFinishEngrave());
					String strFinishTime = (time.format(finishEngrave));
					mav.addObject("finishTime", strFinishTime);
				} catch (ParseException e) {
				}
			} else {// すでに打刻している場合の打刻時間を送る処理
				if (attendanceList.size() != 0) {
					error = "まずは出勤打刻を行ってください。";
				} else {
					error = "本日の退勤打刻は完了しています。";
				}
				mav.addObject("error", error);
				SimpleDateFormat time = new SimpleDateFormat("kk:mm");
				attendance = attendanceList.get(0);
				Date startEngrave;
				try {
					startEngrave = time.parse(attendance.getStartEngrave());
					String strStartTime = (time.format(startEngrave));
					mav.addObject("startTime", strStartTime);
				} catch (ParseException e) {
				}

				Date finishEngrave;
				try {
					finishEngrave = time.parse(attendance.getFinishEngrave());
					String strFinishTime = (time.format(finishEngrave));
					mav.addObject("finishTime", strFinishTime);
				} catch (ParseException e) {
				}
			}

			// リダイレクト先を指定
			mav.setViewName(cmd);

			// ModelとView情報を返す
			return mav;
		} catch (Exception e) {
			mav.setViewName("redirect:/logout");
			mav.addObject("error", "DB接続エラーです。DBを確認してください。");
			return mav;
		}
	}

	/*
	 * ログインの処理 セッションの設定とログイン履歴の登録
	 */
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public ModelAndView login(ModelAndView mav, @RequestParam("password") String pass,
			@RequestParam("employee_id") String id) {
		try {
//		ユーザーリストの取得
			ArrayList<User> list = (ArrayList<User>) userinfo.findAll();
			User user = new User();
			LoginLog log = new LoginLog();
			String cmd = "";

			if (pass.equals("") || id.equals("")) {
				mav.addObject("error", "社員番号またはパスワードが入力されていません。");
				mav.setViewName("login");
				return mav;
			}

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

			if (cmd.equals("")) {
//			エラーがあったらログインに戻る
				mav.addObject("error", "社員番号またはパスワードが間違っています。");
				mav.setViewName("login");
			} else {
//			OKになってたら当日の勤怠情報を検索してメニューに行く
//			ログインした社員の勤怠情報の取得
				Date date = new Date();
				SimpleDateFormat day = new SimpleDateFormat("yyyyMMdd");
				String employeeId = user.getEmployeeId();
				ArrayList<Attendance> attendanceList = (ArrayList<Attendance>) attendanceinfo
						.findByDayAndEmployeeId(day.format(date), employeeId);
				Attendance attendance = new Attendance();
				if (attendanceList.size() != 0) {
//			ログイン日の出勤情報があった場合
					attendance = attendanceList.get(0);
					SimpleDateFormat time = new SimpleDateFormat("kk:mm");
					Date startEngrave;
					Date finishEngrave;
					try {
						if (attendance.getStartEngrave() != null) {
							startEngrave = time.parse(attendance.getStartEngrave());
							mav.addObject("startTime", time.format(startEngrave));
						}
						if (attendance.getFinishEngrave() != null) {
							finishEngrave = time.parse(attendance.getFinishEngrave());
							mav.addObject("finishTime", time.format(finishEngrave));
						}
					} catch (ParseException e) {
					}

				}
				mav.addObject("user", user);
				if (user.getAuthority().equals("1")) {

					mav.setViewName("employeeMenu");
				} else {
					mav.setViewName("adminMenu");
				}
			}
			return mav;
		} catch (Exception e) {
			mav.setViewName("redirect:/logout");
			mav.addObject("error", "DB接続エラーです。DBを確認してください。");
			return mav;
		}
	}

	/*
	 * 社員一覧表示 管理者用機能
	 */
	@RequestMapping("/employeeList")
	public ModelAndView employeeList(
			@RequestParam(value = "message", defaultValue = "", required = false) String message, ModelAndView mav) {
		try {
			//セッション情報の確認
			User user = (User) session.getAttribute("user");
			if (user == null) {
				mav.addObject("error","セッションが切れました。再度ログインしてください。");
				mav.setViewName("login");
				return mav;
			}
//		DBから社員リストを取得
			ArrayList<User> userList = new ArrayList<User>();
			userList = (ArrayList<User>) userinfo.findAll();
//		リストが空でなければmavに登録
			if (userList.size() != 0) {

				// 写真を表示できる形に変換する処理
				for (int i = 0; i < userList.size(); i++) {
					User userPhoto = userList.get(i);
					if (userPhoto.getPhoto() != null) {
						userPhoto.setPhoto(photoView(userPhoto.getPhoto()));
						userList.set(i, userPhoto);
					}
				}

				mav.addObject("employeeList", userList);
			}
			// 変更履歴登録画面から来た場合の処理
			if (!message.equals("")) {
				mav.addObject("message", message);
			}
//		遷移先の指定
			mav.setViewName("employeeList");
			return mav;
		} catch (Exception e) {
			mav.setViewName("redirect:/logout");
			mav.addObject("error", "DB接続エラーです。DBを確認してください。");
			return mav;
		}
	}

	/*
	 * 社員一覧検索機能
	 */
	@RequestMapping("/searchEmployee")
	public ModelAndView search(ModelAndView mav, @RequestParam(value = "employeeId", defaultValue = "") String id,
			@RequestParam(value = "name", defaultValue = "") String name) {
		try {
			//セッション情報の確認
			User user = (User) session.getAttribute("user");
			if (user == null) {
				mav.addObject("error","セッションが切れました。再度ログインしてください。");
				mav.setViewName("login");
				return mav;
			}
			// メッセージを格納する変数
			String message = "";

			String error;
//			DBから条件付きで社員リストを取得
			ArrayList<User> userList = new ArrayList<User>();
			// 社員番号と名前が入力されている
			if (!name.equals("") && !id.equals("")) {
				userList = userinfo.findByEmployeeIdAndName(id, name);
				message = "社員番号 : " + id + " 名前 : " + name + "の検索結果";
			} else if (!name.equals("")) {
				// 名前のみ
				userList = userinfo.findByName(name);
				message = "名前 : " + name + "の検索結果";
			} else if (!id.equals("")) {
				// 社員番号のみ
				userList = userinfo.findByEmployeeIdStartingWith(id);
				message = "社員番号 : " + id + "の検索結果";
			}

//		リストが空でなければmavに登録
			if (userList.size() != 0) {

				// 写真を表示できる形に変換する処理
				for (int i = 0; i < userList.size(); i++) {
					User userPhoto = userList.get(i);
					if (userPhoto.getPhoto() != null) {
						userPhoto.setPhoto(photoView(userPhoto.getPhoto()));
						userList.set(i, userPhoto);
					}
				}

				mav.addObject("employeeList", userList);
			} else {
//			検索結果が空の場合
				error = "検索内容に該当する社員はいませんでした。";
				mav.addObject("error", error);
			}
//		遷移先の指定
			mav.addObject("message", message);
			mav.setViewName("employeeList");
			return mav;
		} catch (Exception e) {
			mav.setViewName("redirect:/logout");
			mav.addObject("error", "DB接続エラーです。DBを確認してください。");
			return mav;
		}
	}

	/*
	 * 管理者登録削除
	 */
	@RequestMapping("/changeAuthority")
	public ModelAndView changeAdmin(ModelAndView mav, @RequestParam("authority") String authority,
			@RequestParam("employeeId") String id) {
		try {
			//セッション情報の確認
			User sessionUser = (User) session.getAttribute("user");
			if (sessionUser == null) {
				mav.addObject("error","セッションが切れました。再度ログインしてください。");
				mav.setViewName("login");
				return mav;
			}
			// メッセージを格納する変数
			String message = "";

			User user = userinfo.findByEmployeeId(id);

			if (authority.equals("0")) {
				user.setAuthority("1");
				message = user.getEmployeeId() + "の権限を一般社員にしました";
			} else {
				user.setAuthority("0");
				message = user.getEmployeeId() + "の権限を管理者にしました";
			}

			userinfo.saveAndFlush(user);

			mav.addObject("message", message);
			mav.setViewName("redirect:/employeeList");
			return mav;
		} catch (Exception e) {
			mav.setViewName("redirect:/logout");
			mav.addObject("error", "DB接続エラーです。DBを確認してください。");
			return mav;
		}
	}

	/*
	 * ログアウト処理
	 * 
	 */
	@RequestMapping("/logout")
	public ModelAndView logout(ModelAndView mav, @ModelAttribute("error") String error) {
		if (error.equals("")) {
			mav.addObject("message", "ログアウトしました");
		} else {
			mav.addObject("error", error);
		}
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
			@RequestParam(value = "month", defaultValue = "", required = false) String month,
			@RequestParam(value = "message", defaultValue = "", required = false) String forMessage, ModelAndView mav) {
		try {
			// メッセージを格納する変数
			String message = "";
			// エラーメッセージを格納する変数
			String error = "";

			// セッション情報の確認
			User user = (User) session.getAttribute("user");
			if (user == null) {
				mav.addObject("error", "セッションが切れました。再度ログインしてください。");
				mav.setViewName("login");
				return mav;
			}

			String employeeId = user.getEmployeeId();
			String authority = user.getAuthority();

//			検索時の入力内容の確認
			int check = 0;
			if (!year.equals("") || !month.equals("")) {
				try {
//						年の文字入力をしていないかチェック
					if (!year.equals("")) {
						check = Integer.parseInt(year);
						check = 0;
//							年の桁数チェック
						if (year.length() != 0 && year.length() != 4) {
							check += 1;
						}
					}
//					月の範囲と文字入力のチェック
					if (!month.equals("")) {
						if (Integer.parseInt(month) > 12 || Integer.parseInt(month) < 1) {
							check += 2;
						}
					}
//				検索にエラーがあったらエラー文を分ける分岐
					switch (check) {
					case 0:
						break;
					case 1:
						error = "年は４桁で入力してください";
						break;
					case 2:
						error = "月は１～１２月の間で入力してください";
						break;
					case 3:
						error = "年は４桁で、月は１～１２月の間で入力してください";
						break;
					}
				} catch (NumberFormatException e) {
					error = "検索の年月は半角数字でお願いします";
				} finally {
//						検索の入力内容にエラーがあった場合
					if (!error.equals("")) {
						mav.addObject("error", error);
						mav.addObject("message", message);
						// 管理者が見る場合
						if (authority.equals("0")) {
							mav.addObject("employeeId", user.getEmployeeId());
						}
						mav.addObject("authority", authority);
						mav.setViewName("attendanceRecord");
						return mav;
					}
				}
			}

			// 月が1桁で入力された場合の処理
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
				if (!forMessage.equals("")) {
					message = forMessage;
				}
			} else if (!year.equals("") && !month.equals("")) {// 年と月で検索された場合
				message = year + "年" + month + "月での検索結果";

				day = year + "-" + month + "%";
				mav.addObject("month", year + "年" + month + "月");
			} else if (year.equals("") && !month.equals("")) {// 月で検索した場合
				message = month + "月での検索結果";

				SimpleDateFormat yearData = new SimpleDateFormat("yyyy");
				day = yearData.format(date) + "-" + month + "%";
				mav.addObject("month", yearData.format(date) + "年" + month + "月");
			} else {// 年で検索した場合
				message = year + "年での検索結果";

				day = year + "%";
				mav.addObject("month", year + "年");
			}
			// ユーザーの勤怠情報の受け取り
			ArrayList<Attendance> attendanceList = attendanceinfo.findByEmployeeIdAndDayLike(employeeId, day);

			// 検索結果があるかどうかの確認
			if (attendanceList.size() == 0 && !year.equals("") || !month.equals("")) {
				error = "検索結果が存在しません。";
			}
				
			String strFormat = ("dd日");
			if (!year.equals("") && month.equals("")) {
				strFormat = "MM月dd日";
			}
			// 形の変換のためのフォーマット
			SimpleDateFormat time = new SimpleDateFormat("HH時mm分");
			SimpleDateFormat timer = new SimpleDateFormat("HH時間mm分");
			SimpleDateFormat days = new SimpleDateFormat(strFormat);
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
					// 出勤時間
					startTime = timeFormat.parse(attendance.getStartTime());
					attendance.setStartTime(time.format(startTime));
					// 退勤打刻時間
					if (attendance.getFinishEngrave() != null) {
						finishEngrave = timeFormat.parse(attendance.getFinishEngrave());
						attendance.setFinishEngrave(time.format(finishEngrave));
					}
					// 退勤時間
					if (attendance.getFinishTime() != null) {
						finishTime = timeFormat.parse(attendance.getFinishTime());
						attendance.setFinishTime(time.format(finishTime));
					}
					// 休憩時間
					if (attendance.getBreakTime() != null) {
						breakTime = timeFormat.parse(attendance.getBreakTime());
						attendance.setBreakTime(timer.format(breakTime));
					}
					// 残業時間
					if (attendance.getOverTime() != null) {
						overTime = timeFormat.parse(attendance.getOverTime());
						attendance.setOverTime(timer.format(overTime));
					}
					// 日付
					dayData = dayFormat.parse(attendance.getDay());
					attendance.setDay(days.format(dayData));

					// データの格納
					attendanceList.set(i, attendance);
				} catch (ParseException e) {
				}
			}

			// 情報の受け渡し
			mav.addObject("attendanceList", attendanceList);
			if (!message.equals("")) {
				mav.addObject("message", message);
			}
			if (!error.equals("")) {
				mav.addObject("error", error);
			}

			mav.addObject("authority", authority);

			// 管理者が見る場合
			if (authority.equals("0")) {
				mav.addObject("employeeId", user.getEmployeeId());
			}

			// 遷移先の指定
			mav.setViewName("attendanceRecord");

			return mav;
		} catch (Exception e) {
			mav.setViewName("redirect:/logout");
			mav.addObject("error", "DB接続エラーです。DBを確認してください。");
			return mav;
		}
	}

	/*
	 * ユーザー一覧から来た場合の勤怠情報確認機能
	 */
	@RequestMapping(value = "/adminAttendanceRecord")
	public ModelAndView adminAttendanceRecorde(
			@RequestParam(value = "year", defaultValue = "", required = false) String year,
			@RequestParam(value = "month", defaultValue = "", required = false) String month,
			@RequestParam(value = "employeeId", defaultValue = "", required = false) String id,
			@RequestParam(value = "message", defaultValue = "", required = false) String forMessage, ModelAndView mav) {
		try {
			// セッション情報の確認
			User user = (User) session.getAttribute("user");
			if (user == null) {
				mav.addObject("error", "セッションが切れました。再度ログインしてください。");
				mav.setViewName("login");
				return mav;
			}
			// メッセージを格納する変数
			String message = "";
			String error = "";

			String authority = user.getAuthority();

			// 月が1桁で入力された場合の処理
			if (month.length() == 1 && month != "") {
				month = "0" + month;
			}

//			検索時の入力内容の確認
			int check = 0;
			if (!year.equals("") || !month.equals("")) {
				try {
//						年の文字入力をしていないかチェック
					if (!year.equals("")) {
						check = Integer.parseInt(year);
						check = 0;
//							年の桁数チェック
						if (year.length() != 0 && year.length() != 4) {
							check += 1;
						}
					}
//					月の範囲と文字入力のチェック
					if (!month.equals("")) {
						if (Integer.parseInt(month) > 12 || Integer.parseInt(month) < 1) {
							check += 2;
						}
					}
//				検索にエラーがあったらエラー文を分ける分岐
					switch (check) {
					case 0:
						break;
					case 1:
						error = "年は４桁で入力してください";
						break;
					case 2:
						error = "月は１～１２月の間で入力してください";
						break;
					case 3:
						error = "年は４桁で、月は１～１２月の間で入力してください";
						break;
					}
				} catch (NumberFormatException e) {
					error = "検索の年月は半角数字でお願いします";
				} finally {
//						検索の入力内容にエラーがあった場合
					if (!error.equals("")) {
						mav.addObject("error", error);
						mav.addObject("message", message);
						// 管理者が見る場合
						if (authority.equals("0")) {
							mav.addObject("employeeId", user.getEmployeeId());
						}
						mav.addObject("authority", authority);
						mav.setViewName("attendanceRecord");
						return mav;
					}
				}
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
				if (!forMessage.equals("")) {
					message = forMessage;
				}
			} else if (!year.equals("") && !month.equals("")) {// 年と月で検索された場合
				message = year + "年" + month + "月での検索結果";

				day = year + "-" + month + "%";
				mav.addObject("month", year + "年" + month + "月");
			} else if (year.equals("") && !month.equals("")) {// 月で検索した場合
				message = month + "月での検索結果";

				SimpleDateFormat yearData = new SimpleDateFormat("yyyy");
				day = yearData.format(date) + "-" + month + "%";
				mav.addObject("month", yearData.format(date) + "年" + month + "月");
			} else {// 年で検索した場合
				message = year + "年での検索結果";

				day = year + "%";
				mav.addObject("month", year + "年");
			}
			// ユーザーの勤怠情報の受け取り
			ArrayList<Attendance> attendanceList = attendanceinfo.findByEmployeeIdAndDayLike(id, day);

			// 検索結果があるかどうかの確認
			if (attendanceList.size() == 0 && !year.equals("") || !month.equals("")) {
				error = "検索結果が存在しません。";
			}
			String strFormat = ("dd日");
			if (!year.equals("") && month.equals("")) {
				strFormat = "MM月dd日";
			}

			// 形の変換のためのフォーマット
			SimpleDateFormat time = new SimpleDateFormat("HH時mm分");
			SimpleDateFormat timer = new SimpleDateFormat("HH時間mm分");
			SimpleDateFormat days = new SimpleDateFormat(strFormat);
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
					// 出勤時間
					startTime = timeFormat.parse(attendance.getStartTime());
					attendance.setStartTime(time.format(startTime));
					// 退勤打刻時間
					if (attendance.getFinishEngrave() != null) {
						finishEngrave = timeFormat.parse(attendance.getFinishEngrave());
						attendance.setFinishEngrave(time.format(finishEngrave));
					}
					// 退勤時間
					if (attendance.getFinishTime() != null) {
						finishTime = timeFormat.parse(attendance.getFinishTime());
						attendance.setFinishTime(time.format(finishTime));
					}
					// 休憩時間
					if (attendance.getBreakTime() != null) {
						breakTime = timeFormat.parse(attendance.getBreakTime());
						attendance.setBreakTime(timer.format(breakTime));
					}
					// 残業時間
					if (attendance.getOverTime() != null) {
						overTime = timeFormat.parse(attendance.getOverTime());
						attendance.setOverTime(timer.format(overTime));
					}
					// 日付
					dayData = dayFormat.parse(attendance.getDay());
					attendance.setDay(days.format(dayData));

					// データの格納
					attendanceList.set(i, attendance);
				} catch (ParseException e) {
				}
			}

			// 情報の受け渡し
			mav.addObject("attendanceList", attendanceList);
			mav.addObject("authority", authority);
			mav.addObject("employeeId", id);
			if (!message.equals("")) {
				mav.addObject("message", message);
			}
			if (!error.equals("")) {
				mav.addObject("error", error);
			}

			// 遷移先の指定
			mav.setViewName("attendanceRecord");

			return mav;
		} catch (

		Exception e) {
			mav.setViewName("redirect:/logout");
			mav.addObject("error", "DB接続エラーです。DBを確認してください。");
			return mav;
		}
	}

	/*
	 * 社員登録処理
	 */
	@RequestMapping(value = "/employeeRegistration", method = RequestMethod.POST)
	public ModelAndView employeeRegistration(
			@RequestParam(value = "name", defaultValue = "", required = false) String name,
			@RequestParam(value = "employeeId", defaultValue = "", required = false) String employeeId,
			@RequestParam(value = "oldEmployeeId", defaultValue = "", required = false) String oldEmployeeId,
			@RequestParam(value = "password", defaultValue = "", required = false) String password,
			@RequestParam(value = "email", defaultValue = "", required = false) String email, MultipartFile photo,
			@RequestParam(value = "authority", defaultValue = "", required = false) String authority,
			ModelAndView mav) {
		try {
			//セッション情報の確認
			User user = (User) session.getAttribute("user");
			if (user == null) {
				mav.addObject("error","セッションが切れました。再度ログインしてください。");
				mav.setViewName("login");
				return mav;
			}
			// メッセージを格納する変数
			String message = "";

//		入力データの確認
			if (name.equals("") || password.equals("") || employeeId.equals("") || email.equals("")) {
				mav.addObject("error", "名前、パスワード、社員番号、Emailアドレスが入力されていません。");
				mav.setViewName("employeeRegistration");
				return mav;
			}
			if (password.length() < 8) {
				mav.addObject("error", "パスワードは最低でも８桁設定してください");
				mav.setViewName("employeeRegistration");
				return mav;
			}
			User check = userinfo.findByEmployeeId(employeeId);
			if (check != null) {
				if (!check.getEmployeeId().equals(user.getEmployeeId())) {
					if (check.getIsDeleted() == false) {
						mav.addObject("error", "社員番号が重複しています。");
						mav.addObject("user", user);
						mav.setViewName("employeeRegistration");
						return mav;
					}
				}
			}

//		写真の取り込み処理
			if (!(photo.getOriginalFilename().equals(""))) {
				try {
					// ファイル名を社員番号に変更
					File oldFileName = new File(photo.getOriginalFilename());
					File newFileName = new File(employeeId + ".jpg");
					oldFileName.renameTo(newFileName);

					// 保存先を定義
					String uploadPath = "photo/";
					byte[] buytes = photo.getBytes();

					// 指定ファイルへ読み込みファイルを書き込む
					BufferedOutputStream stream = new BufferedOutputStream(
							new FileOutputStream(new File(uploadPath + newFileName)));
					stream.write(buytes);
					stream.close();

					// 圧縮
					File input = new File(uploadPath + newFileName);
					BufferedImage image = ImageIO.read(input);
					OutputStream os = new FileOutputStream(input);
					Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
					ImageWriter writer = (ImageWriter) writers.next();
					ImageOutputStream ios = ImageIO.createImageOutputStream(os);
					writer.setOutput(ios);
					ImageWriteParam param = new JPEGImageWriteParam(null);
					param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
					param.setCompressionQuality(0.30f);
					writer.write(null, new IIOImage(image, null, null), param);
					os.close();
					ios.close();
					writer.dispose();

				} catch (IOException e) {
					System.out.println(e);
				}
			}

			user.setName(name);
			user.setEmployeeId(employeeId);
			user.setPassword(password);
			user.setEmail(email);
			if (!(photo.getOriginalFilename().equals(""))) {
				user.setPhoto(employeeId + ".jpg");
			}
			user.setAuthority(authority);

//		入力データをDBに保存
			userinfo.saveAndFlush(user);

			message = "社員を登録しました";

			mav.addObject("message", message);

			mav.setViewName("redirect:/employeeList");
// 		ModelとView情報を返す
			return mav;
		} catch (Exception e) {
			mav.setViewName("redirect:/logout");
			mav.addObject("error", "DB接続エラーです。DBを確認してください。");
			return mav;
		}
	}
	/*
	 * 勤怠情報変更
	 */
	@RequestMapping(value = "/changeAttendance", method = RequestMethod.POST)
	public ModelAndView changeAttendance(
			@RequestParam(value = "attendanceId", defaultValue = "", required = false) String attendanceId,
			@RequestParam(value = "startTime", defaultValue = "", required = false) String startTime,
			@RequestParam(value = "finishTime", defaultValue = "", required = false) String finishTime,
			@RequestParam(value = "overTime", defaultValue = "", required = false) String overTime,
			@RequestParam(value = "breakTime", defaultValue = "", required = false) String breakTime,
			@RequestParam(value = "startEngrave", defaultValue = "", required = false) String startEngrave,
			@RequestParam(value = "finishEngrave", defaultValue = "", required = false) String finishEngrave,
			@RequestParam(value = "employeeId", defaultValue = "", required = false) String employeeId,
			@RequestParam(value = "day", defaultValue = "", required = false) String day, ModelAndView mav,
			RedirectAttributes redirectAttributes) {
		try {
			// メッセージを格納する変数
			String message = "";

//			//セッション情報の確認
			User user = (User) session.getAttribute("user");
			if (user == null) {
				mav.addObject("error", "セッションが切れました。再度ログインしてください。");
				mav.setViewName("login");
				return mav;
			}

//			入力内容の確認
			if (!finishTime.equals("00:00:00")) {
				int start = Integer.parseInt(startTime.replace(":", ""));
				int fin = Integer.parseInt(finishTime.replace(":", ""));
				int btime = Integer.parseInt(breakTime.replace(":", ""));
//			文字列の長さを見て変更しているか確認.
//			変更していたら未変更のものと桁を合わせるために100かける。(hhmmssにそろえる)
//			出勤時間を変更していた場合
				if (startTime.length() == 5) {
					start *= 100;
				}
//			退勤時間を変更していた場合
				if (finishTime.length() == 5) {
					fin *= 100;
				}
				if (breakTime.length() == 5) {
					btime *= 100;
				}

				if (start > fin) {
					mav.addObject("error", "退勤時間が出勤時間よりも早くなっています");
					mav.addObject("employeeId", employeeId);
					mav.addObject("attendanceId", attendanceId);
					mav.setViewName("redirect:/changeAttendanceinfo");
					return mav;
				}
				if (start == fin) {
					mav.addObject("error", "退勤時間と出勤時間が同じ時間です");
					mav.addObject("employeeId", employeeId);
					mav.addObject("attendanceId", attendanceId);
					mav.setViewName("redirect:/changeAttendanceinfo");
					return mav;
				}
				if (btime >= fin - start) {
					mav.addObject("error", "休憩時間が長すぎます");
					mav.addObject("employeeId", employeeId);
					mav.addObject("attendanceId", attendanceId);
					mav.setViewName("redirect:/changeAttendanceinfo");
					return mav;
				}
			}
//			変更前勤怠情報の取得
			Attendance before = attendanceinfo.findByAttendanceId(Integer.parseInt(attendanceId));
			if (before.getFinishTime() == null) {
				before.setFinishTime("00:00:00");
			}
			if (before.getFinishEngrave() == null) {
				before.setFinishEngrave("00:00:00");
			}
			if (before.getBreakTime() == null) {
				before.setBreakTime("00:00:00");
			}
			if (before.getOverTime() == null) {
				before.setOverTime("00:00:00");
			}

//			変更後勤怠情報のオブジェクト化
			Attendance after = new Attendance();
			after.setAttendanceId(Integer.parseInt(attendanceId));
			after.setEmployeeId(before.getEmployeeId());
			after.setDay(day);
			after.setStartTime(startTime);
			after.setStartEngrave(startEngrave);
			if (!finishTime.equals("00:00:00")) {
				after.setFinishEngrave(finishEngrave);
				after.setFinishTime(finishTime);
				after.setOverTime(overTime);
				after.setBreakTime(breakTime);
			}

//			勤怠情報が全く変更されていないときはエラー
//			出退勤が行われているとき
			if (after.getFinishTime() != null) {
				if (before.getStartTime().equals(after.getStartTime())
						&& before.getFinishTime().equals(after.getFinishTime())
						&& before.getOverTime().equals(after.getOverTime())
						&& before.getBreakTime().equals(after.getBreakTime())) {
					mav.addObject("error", "勤怠情報が変更されていません");
					mav.addObject("employeeId", employeeId);
					mav.addObject("attendanceId", attendanceId);
					mav.setViewName("redirect:/changeAttendanceinfo");
					return mav;
				}
			} else {
//			出勤のみの情報の時
				if (before.getStartTime().equals(after.getStartTime())) {
					mav.addObject("error", "勤怠情報が変更されていません");
					mav.addObject("employeeId", employeeId);
					mav.addObject("attendanceId", attendanceId);
					mav.setViewName("redirect:/changeAttendanceinfo");
					return mav;
				}
			}

			ArrayList<Change> list = new ArrayList<Change>();

			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
			String time = sdf.format(date);

//		それぞれの勤怠情報について変更履歴の登録
//		出勤時間
			if (!before.getStartTime().equals(after.getStartTime())) {
				Change change = new Change();
				change.setAdminId(user.getEmployeeId());
				change.setIsUpdated(time);
				change.setEmployeeId(before.getEmployeeId());
				change.setDataName(before.getDay() + "の出勤時間");
				change.setBeforeData(before.getStartTime());
				change.setAfterData(after.getStartTime());
				list.add(change);
			}
//			退勤時刻の入力がないときは比較を行わない
			if (!finishTime.equals("00:00:00")) {
//			退勤時間
				if (!before.getFinishTime().equals(after.getFinishTime())) {
					Change change = new Change();
					change.setAdminId(user.getEmployeeId());
					change.setIsUpdated(time);
					change.setEmployeeId(before.getEmployeeId());
					change.setDataName(before.getDay() + "の退勤時間");
					if (before.getFinishTime().equals("00:00:00")) {
						change.setBeforeData("NULL");
					} else {
						change.setBeforeData(before.getFinishTime());
					}
					change.setAfterData(after.getFinishTime());
					list.add(change);
				}
//			休憩時間
				if (!before.getBreakTime().equals(after.getBreakTime())) {
					Change change = new Change();
					change.setAdminId(user.getEmployeeId());
					change.setIsUpdated(time);
					change.setEmployeeId(before.getEmployeeId());
					change.setDataName(before.getDay() + "の休憩時間");
					if (before.getBreakTime().equals("00:00:00")) {
						change.setBeforeData("NULL");
					} else {
						change.setBeforeData(before.getBreakTime());
					}
					change.setAfterData(after.getBreakTime());
					list.add(change);
				}
//			残業時間
				if (!before.getOverTime().equals(after.getOverTime())) {
					Change change = new Change();
					change.setAdminId(user.getEmployeeId());
					change.setIsUpdated(time);
					change.setEmployeeId(before.getEmployeeId());
					change.setDataName(before.getDay() + "の残業時間");
					if (before.getOverTime().equals("00:00:00")) {
						change.setBeforeData("NULL");
					} else {
						change.setBeforeData(before.getOverTime());
					}
					change.setAfterData(after.getOverTime());
					list.add(change);
				}
			}

//		勤怠情報変更の登録
			attendanceinfo.saveAndFlush(after);
//		遷移先コントローラーの選択
			String cmd;
			if (user.getEmployeeId().equals(employeeId)) {
				// 管理者が自身の勤怠情報を変更したとき
				cmd = "redirect:/attendanceRecord";
			} else {
				// 管理者が他社員の勤怠情報を変更したとき
				mav.addObject("employeeId", after.getEmployeeId());
				cmd = "redirect:/adminAttendanceRecord";
				mav.addObject("employeeId", employeeId);
			}

			message = "勤怠情報を変更しました";

//		変更情報をMapに登録
			ModelMap md = new ModelMap();
			md.addAttribute("changeList", list);
			redirectAttributes.addFlashAttribute("map1", md);
			redirectAttributes.addFlashAttribute("message", message);
			redirectAttributes.addFlashAttribute("move", cmd);

			mav.setViewName("redirect:/changeInsert");
			return mav;
		} catch (Exception e) {
			mav.setViewName("redirect:/logout");
			mav.addObject("error", "DB接続エラーです。DBを確認してください。");
			return mav;
		}
	}

	/*
	 * リクエスト情報の受け取り
	 */
	@RequestMapping("/changeRequestList")
	public ModelAndView changeRequestList(ModelAndView mav,
			@RequestParam(value = "message", defaultValue = "", required = false) String message) {
		try {
			//セッション情報の確認
			User user = (User) session.getAttribute("user");
			if (user == null) {
				mav.addObject("error","セッションが切れました。再度ログインしてください。");
				mav.setViewName("login");
				return mav;
			}
			// requestInfoの情報をすべて受け取る
			ArrayList<Request> requestList = (ArrayList<Request>) requestinfo.findAll();

			// 日付を含めたリクエスト情報を格納する変数
			ArrayList<RequestDay> requestDayList = new ArrayList<RequestDay>();
			// 日付の形を変更する処理
			SimpleDateFormat dayType = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy年MM月dd日");

			for (int i = 0; i < requestList.size(); i++) {
				Request request = requestList.get(i);
				if (request.getIsDeleted() == false) {
					RequestDay requestDay = new RequestDay();
					// 勤怠情報IDから日付を検索する
					Attendance attendance = attendanceinfo.findByAttendanceId(request.getAttendanceId());

					try {
						// 日付のフォーマットを変更する
						Date day = dayType.parse(attendance.getDay());
						requestDay.setDay(dayFormat.format(day));
					} catch (ParseException e) {

					}

					requestDay.setRequestId(request.getRequestId());
					requestDay.setEmployeeId(request.getEmployeeId());
					requestDay.setAttendanceId(request.getAttendanceId());
					requestDay.setBeforeStartTime(attendance.getStartTime());
					requestDay.setChangeStartTime(request.getChangeStartTime());
					if (attendance.getFinishTime() == null) {
						requestDay.setBeforeFinishTime("なし");
						requestDay.setChangeFinishTime("なし");
					} else {
						requestDay.setBeforeFinishTime(attendance.getFinishTime());
						requestDay.setChangeFinishTime(request.getChangeFinishTime());
					}
					requestDay.setComment(request.getComment());

					requestDayList.add(requestDay);
				}
			}

			if (!message.equals("")) {
				mav.addObject("message", message);
			}

			mav.addObject("requestDayList", requestDayList);

			mav.setViewName("changeRequestList");

			return mav;
		} catch (Exception e) {
			mav.setViewName("redirect:/logout");
			mav.addObject("error", "DB接続エラーです。DBを確認してください。");
			return mav;
		}
	}

	/*
	 * リクエストされた情報を変更するかリクエストを削除する
	 */
	@RequestMapping(value = "/changeRequest", method = RequestMethod.POST)
	public ModelAndView changeRequest(RedirectAttributes redirectAttributes,
			@RequestParam(value = "requestId") String strRequestId, @RequestParam(value = "cmd") String cmd,
			ModelAndView mav) {
		try {
			// メッセージを格納する変数
			String message = "";

			//セッション情報の確認
			User user = (User) session.getAttribute("user");
			if (user == null) {
				mav.addObject("error","セッションが切れました。再度ログインしてください。");
				mav.setViewName("login");
				return mav;
			}

			// 対象のリクエスト情報を受け取る
			int requestId = Integer.parseInt(strRequestId);
			Request request = requestinfo.findByRequestId(requestId);

			// 勤怠情報IDから対象の勤怠情報を検索する
			Attendance attendance = attendanceinfo.findByAttendanceId(request.getAttendanceId());

			// 対象のリクエスト情報を管理する変数の宣言と情報の登録
			RequestDay requestDay = new RequestDay();
			requestDay.setRequestId(request.getRequestId());
			requestDay.setEmployeeId(request.getEmployeeId());
			requestDay.setAttendanceId(request.getAttendanceId());
			requestDay.setBeforeStartTime(attendance.getStartTime());
			requestDay.setBeforeFinishTime(attendance.getFinishTime());
			requestDay.setChangeStartTime(request.getChangeStartTime());
			requestDay.setChangeFinishTime(request.getChangeFinishTime());
			requestDay.setComment(request.getComment());

			// 変更履歴を保存する変数の宣言
			ArrayList<Change> changeList = new ArrayList<Change>();

			// 日付の受け取り
			SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy/MM/dd kk:mm:ss");
			Date date = new Date();

			// 認可した場合の処理
			if (cmd.equals("input")) {

				// 情報を変更するかのif文
				if (request.getChangeStartTime() != null
						&& !requestDay.getBeforeStartTime().equals(request.getChangeStartTime())) {// 出勤時間を変更する場合
					attendance.setStartTime(request.getChangeStartTime());

					// 変更履歴を追加
					Change change = new Change();
					change.setAdminId(user.getEmployeeId());
					change.setDataName(attendance.getDay() + "出勤時間");
					change.setBeforeData(requestDay.getBeforeStartTime());
					change.setAfterData(requestDay.getChangeStartTime());
					change.setEmployeeId(requestDay.getEmployeeId());
					change.setIsUpdated(dayFormat.format(date));

					changeList.add(change);
					// changeinfo.saveAndFlush(change);
				}
				if (request.getChangeFinishTime() != null
						&& !requestDay.getBeforeFinishTime().equals(request.getChangeFinishTime())) {// 退勤時間を変更する場合
					attendance.setFinishTime(request.getChangeFinishTime());

					// 変更履歴を追加
					Change change = new Change();
					change.setAdminId(user.getEmployeeId());
					change.setDataName(attendance.getDay() + "退勤時間");
					change.setBeforeData(requestDay.getBeforeFinishTime());
					change.setAfterData(requestDay.getChangeFinishTime());
					change.setEmployeeId(requestDay.getEmployeeId());
					change.setIsUpdated(dayFormat.format(date));

					changeList.add(change);
				}

				if(!attendance.getFinishTime().equals("")) {
					// 勤務時間・休憩時間・残業時間の設定
					SimpleDateFormat timeFormat = new SimpleDateFormat("kk:mm:ss");
					SimpleDateFormat time = new SimpleDateFormat("kk:mm");
					try {
						Date startTime = timeFormat.parse(requestDay.getChangeStartTime());
						attendance.setStartTime(time.format(startTime));
						Date finishTime = timeFormat.parse(requestDay.getChangeFinishTime());
						attendance.setFinishTime(time.format(finishTime));
					} catch (ParseException e) {
					}
	
					// 勤務時間（時間）の計算
					int hour = Integer.parseInt(attendance.getFinishTime().substring(0, 2))
							- Integer.parseInt(attendance.getStartTime().substring(0, 2));
					// 勤務時間（分）の計算
					int minute = Integer.parseInt(attendance.getFinishTime().substring(3, 5))
							- Integer.parseInt(attendance.getStartTime().substring(3, 5));
					if (minute < 0) {
						minute = 60 + minute;
						hour = hour - 1;
					}
					// 休憩時間・残業時間の計算と格納
					if (hour < 9) {// 8時間未満の勤務の場合 
						attendance.setBreakTime("01:00");
						attendance.setOverTime("00:00");
					} else {// ８時間以上の勤務の場合
						if(hour == 9 && minute <= 15) {//勤務時間が9時間で時間が15分以内の場合
							attendance.setBreakTime("01:" + minute);
							attendance.setOverTime("00:00");
						}else {
							attendance.setBreakTime("01:15");
							// 残業時間の計算
							hour = hour - 9;
							minute = minute - 15;
							if (minute < 0) {
								minute = 60 + minute;
								hour = hour - 1;
							}
							attendance.setOverTime(hour + ":" + minute);
						}
					}
				}

				attendanceinfo.saveAndFlush(attendance);
				message = "勤務情報を変更しました";
			} else {
				message = "勤務情報変更リクエストを拒否しました";
			}

			// 処理を行った変更リクエストを削除する
			request.setIsDeleted(true);
			requestinfo.saveAndFlush(request);

			if (cmd.equals("input")) {
				ModelMap modelMap = new ModelMap();
				modelMap.addAttribute("changeList", changeList);
				redirectAttributes.addFlashAttribute("map1", modelMap);
				redirectAttributes.addFlashAttribute("move", "redirect:/changeRequestList");
				redirectAttributes.addFlashAttribute("message", message);
				mav.setViewName("redirect:/changeInsert");
				return mav;
			} else {
				mav.addObject("message", message);
				mav.setViewName("redirect:/changeRequestList");
				return mav;
			}
		} catch (Exception e) {
			mav.setViewName("redirect:/logout");
			mav.addObject("error", "DB接続エラーです。DBを確認してください。");
			return mav;
		}
	}

	// 変更履歴を登録する
	@RequestMapping("/changeInsert")
	public ModelAndView changeinsert(RedirectAttributes redirectAttributes, @ModelAttribute("map1") ModelMap map1,
			@ModelAttribute("message") String message, @ModelAttribute("move") String move,
			@RequestParam(value = "cmd", defaultValue = "") String cmd,
			@RequestParam(value = "oldEmployeeId", defaultValue = "") String oldEmployeeId,
			@RequestParam(value = "employeeId", defaultValue = "") String employeeId, ModelAndView mav) {
		try {
			ArrayList<Change> changeList = (ArrayList<Change>) map1.get("changeList");
			changeinfo.saveAndFlush(changeList.get(0));
			changeList.remove(0);

			ModelMap modelMap = new ModelMap();
			modelMap.addAttribute("changeList", changeList);
			redirectAttributes.addFlashAttribute("map1", modelMap);
			redirectAttributes.addFlashAttribute("move", move);
			redirectAttributes.addFlashAttribute("message", message);
			if (cmd.equals("change")) {
				mav.addObject("cmd", cmd);
				mav.addObject("oldEmployeeId", oldEmployeeId);
			}

			if (changeList.size() == 0) {
				// 管理者が勤怠情報変更を行い他社員の勤怠情報一覧に戻るとき
				if (move.equals("redirect:/adminAttendanceRecord")) {
					mav.addObject("employeeId", employeeId);
				} else {
					mav.addObject("employeeId", oldEmployeeId);
				}
				mav.addObject("message", message);
				mav.setViewName(move);
				return mav;
			} else {
				mav.addObject("employeeId", employeeId);
				mav.setViewName("redirect:/changeInsert");
				return mav;
			}
		} catch (Exception e) {
			mav.setViewName("redirect:/logout");
			mav.addObject("error", "DB接続エラーです。DBを確認してください。");
			return mav;
		}
	}
	/*
	 * 社員情報を送る処理
	 */
	@RequestMapping(value = "/changeEmployee", method = RequestMethod.POST)
	public ModelAndView changeEmployeeInfo(ModelAndView mav, @RequestParam(value = "employeeId") String empid) {
		try {
			//セッション情報の確認
			User sessionUser = (User) session.getAttribute("user");
			if (sessionUser == null) {
				mav.addObject("error","セッションが切れました。再度ログインしてください。");
				mav.setViewName("login");
				return mav;
			}

			User user = userinfo.findByEmployeeId(empid);

			// 写真を表示できる形に変換する処理
			if (user.getPhoto() != null) {
				user.setPhoto(photoView(user.getPhoto()));
			}

			mav.addObject("user", user);
			mav.setViewName("changeEmployeeInfo");
			return mav;
		} catch (Exception e) {
			mav.setViewName("redirect:/logout");
			mav.addObject("error", "DB接続エラーです。DBを確認してください。");
			return mav;
		}

	}

	/*
	 * 社員情報変更処理
	 */
	@RequestMapping(value = "/changeEmployeeInfo", method = RequestMethod.POST)
	public ModelAndView changeEmployeeInfoPost(
			@RequestParam(value = "name", defaultValue = "", required = false) String name,
			@RequestParam(value = "employeeId", defaultValue = "", required = false) String employeeId,
			@RequestParam(value = "oldEmployeeId", defaultValue = "", required = false) String oldEmployeeId,
			@RequestParam(value = "password", defaultValue = "", required = false) String password,
			@RequestParam(value = "email", defaultValue = "", required = false) String email, MultipartFile photo,
			@RequestParam(value = "authority", defaultValue = "", required = false) String authority,
			RedirectAttributes redirectAttributes, ModelAndView mav) {
		try {
			//セッション情報の確認
			User sessionUser = (User) session.getAttribute("user");
			if (sessionUser == null) {
				mav.addObject("error","セッションが切れました。再度ログインしてください。");
				mav.setViewName("login");
				return mav;
			}
			// メッセージを格納する変数
			String message = "";

			User user = new User();
			User before = userinfo.findByEmployeeId(oldEmployeeId);

//		入力内容の判定
			if (name.equals("") || employeeId.equals("") || password.equals("") || email.equals("")) {
				mav.addObject("error", "社員番号、名前、パスワード、Emailは必ず入力してください。");
				mav.addObject("user", before);
				mav.setViewName("changeEmployeeInfo");
				return mav;
			}
			
			//情報が変更されていない場合
			if (name.equals(before.getName()) && employeeId.equals(before.getEmployeeId()) &&
					password.equals(before.getPassword()) && email.equals(before.getEmail()) && photo.getOriginalFilename().equals("")) {
				mav.addObject("error", "情報を変更してください。");
				mav.addObject("user", before);
				mav.setViewName("changeEmployeeInfo");
				return mav;
			}

			if (password.length() < 8) {
				mav.addObject("error", "パスワードは最低でも８桁設定してください");
				mav.addObject("user", before);
				mav.setViewName("changeEmployeeInfo");
				return mav;
			}

			User check = userinfo.findByEmployeeId(employeeId);
			if (check != null) {
				if (!check.getEmployeeId().equals(before.getEmployeeId())) {
					if (check.getIsDeleted() == false) {
						mav.addObject("error", "社員番号が重複しています。");
						mav.addObject("user", before);
						mav.setViewName("changeEmployeeInfo");
						return mav;
					}
				}
			}

			if (!(photo.getOriginalFilename().equals(""))) {
				try {
					// ファイル名を社員番号に変更
					File oldFileName = new File(photo.getOriginalFilename());
					File newFileName = new File(employeeId + ".jpg");
					oldFileName.renameTo(newFileName);

					// 保存先を定義
					String uploadPath = "photo/";
					byte[] buytes = photo.getBytes();

					// 指定ファイルへ読み込みファイルを書き込む
					BufferedOutputStream stream = new BufferedOutputStream(
							new FileOutputStream(new File(uploadPath + newFileName)));
					stream.write(buytes);
					stream.close();

					// 圧縮
					File input = new File(uploadPath + newFileName);
					BufferedImage image = ImageIO.read(input);
					OutputStream os = new FileOutputStream(input);
					Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
					ImageWriter writer = (ImageWriter) writers.next();
					ImageOutputStream ios = ImageIO.createImageOutputStream(os);
					writer.setOutput(ios);
					ImageWriteParam param = new JPEGImageWriteParam(null);
					param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
					param.setCompressionQuality(0.30f);
					writer.write(null, new IIOImage(image, null, null), param);
					os.close();
					ios.close();
					writer.dispose();

				} catch (IOException e) {
					System.out.println(e);
				}
			}
			user.setName(name);
			user.setEmployeeId(employeeId);
			user.setPassword(password);
			user.setEmail(email);
			user.setAuthority(authority);
			if (!(photo.getOriginalFilename().equals("")) || before.getPhoto() != null) {
				user.setPhoto(employeeId + ".jpg");
			}

//		変更履歴の登録
			ArrayList<Change> list = new ArrayList<Change>();

			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
			String time = sdf.format(date);
			String cmd = "";
//		社員番号
			if (!before.getEmployeeId().equals(user.getEmployeeId())) {
				Change change = new Change();
				change.setAdminId(employeeId);
				change.setIsUpdated(time);
				change.setEmployeeId(before.getEmployeeId());
				change.setDataName("社員番号");
				change.setBeforeData(before.getEmployeeId());
				change.setAfterData(user.getEmployeeId());
				list.add(change);
				cmd = "change";
				mav.addObject("cmd", cmd);
				mav.addObject("oldEmployeeId", before.getEmployeeId());
			}
//		社員名
			if (!before.getName().equals(user.getName())) {
				Change change = new Change();
				change.setAdminId(employeeId);
				change.setIsUpdated(time);
				change.setEmployeeId(before.getEmployeeId());
				change.setDataName("社員名");
				change.setBeforeData(before.getName());
				change.setAfterData(user.getName());
				list.add(change);
			}
//		パスワード
			if (!before.getPassword().equals(user.getPassword())) {
				Change change = new Change();
				change.setAdminId(employeeId);
				change.setIsUpdated(time);
				change.setEmployeeId(before.getEmployeeId());
				change.setDataName("パスワード");
				change.setBeforeData(before.getPassword());
				change.setAfterData(user.getPassword());
				list.add(change);
			}
//		Email
			if (!before.getEmail().equals(user.getEmail())) {
				Change change = new Change();
				change.setAdminId(employeeId);
				change.setIsUpdated(time);
				change.setEmployeeId(before.getEmployeeId());
				change.setDataName("Emailアドレス");
				change.setBeforeData(before.getEmail());
				change.setAfterData(user.getEmail());
				list.add(change);
			}
//		写真
			if (!(photo.getOriginalFilename().equals(""))) {
				if (before.getPhoto() != null) {
					Change change = new Change();
					change.setAdminId(employeeId);
					change.setIsUpdated(time);
					change.setEmployeeId(before.getEmployeeId());
					change.setDataName("写真の変更");
					change.setBeforeData(before.getPhoto());
					change.setAfterData(user.getPhoto());
					list.add(change);
				}else {
					Change change = new Change();
					change.setAdminId(employeeId);
					change.setIsUpdated(time);
					change.setEmployeeId(before.getEmployeeId());
					change.setDataName("写真の追加");
					change.setBeforeData("無し");
					change.setAfterData(user.getPhoto());
					list.add(change);
				}
			}
//		権限
			if (!before.getAuthority().equals(user.getAuthority())) {
				Change change = new Change();
				change.setAdminId(employeeId);
				change.setIsUpdated(time);
				change.setEmployeeId(before.getEmployeeId());
				change.setDataName("権限");
				if (before.getAuthority().equals("0")) {
					change.setBeforeData("管理者");
					change.setAfterData("一般社員");
				} else {
					change.setBeforeData("一般社員");
					change.setAfterData("管理者");
				}
				list.add(change);
			}

//		入力データをDBに保存
			userinfo.saveAndFlush(user);

			message = "社員情報を変更しました。";

//		変更情報をMapに登録
			ModelMap md = new ModelMap();
			md.addAttribute("changeList", list);
			redirectAttributes.addFlashAttribute("map1", md);
			redirectAttributes.addFlashAttribute("message", message);
			if (!cmd.equals("change")) {
				redirectAttributes.addFlashAttribute("move", "redirect:/employeeList");
			} else {
				redirectAttributes.addFlashAttribute("move", "redirect:/deleteEmployee");
			}
			mav.setViewName("redirect:/changeInsert");
			return mav;
		} catch (Exception e) {
			mav.setViewName("redirect:/logout");
			mav.addObject("error", "DB接続エラーです。DBを確認してください。");
			return mav;
		}
	}

	/* 社員削除機能 */
	@RequestMapping("/deleteEmployee")
	public ModelAndView deleteEmployee(@RequestParam("employeeId") String id,
			@RequestParam(value = "cmd", defaultValue = "") String cmd, ModelAndView mav) {
		try {
			//セッション情報の確認
			User sessionUser = (User) session.getAttribute("user");
			if (sessionUser == null) {
				mav.addObject("error","セッションが切れました。再度ログインしてください。");
				mav.setViewName("login");
				return mav;
			}
			User user = userinfo.findByEmployeeId(id);
			if (user == null || user.getIsDeleted()) {
				mav.addObject("error", "削除しようとした社員はすでにDBに存在しません。");

				return mav;
			}
			// メッセージを格納する変数
			String message;
			if (cmd.equals("")) {
				message = "社員を削除しました";
			} else {
				message = "社員情報を更新しました。";
			}
			user.setIsDeleted(true);
			userinfo.saveAndFlush(user);
			mav.addObject("message", message);
			mav.setViewName("redirect:/employeeList");
			return mav;
		} catch (Exception e) {
			mav.setViewName("redirect:/logout");
			mav.addObject("error", "DB接続エラーです。DBを確認してください。");
			return mav;
		}
	}

	/* リクエスト登録 */
	@PostMapping("/requestForm")
	public ModelAndView requestForm(@RequestParam(value = "changeStartTime", defaultValue = "") String changeStartTime,
			@RequestParam(value = "changeFinishTime", defaultValue = "") String changeFinishTime,
			@RequestParam(value = "comment", defaultValue = "") String comment,
			@RequestParam("attendanceId") String attendanceId, @RequestParam("employeeId") String employeeId,
			ModelAndView mav) {
		try {
			//セッション情報の確認
			User user = (User) session.getAttribute("user");
			if (user == null) {
				mav.addObject("error","セッションが切れました。再度ログインしてください。");
				mav.setViewName("login");
				return mav;
			}
//		入力内容の確認
			if (!changeFinishTime.equals("")) {
				int start = Integer.parseInt(changeStartTime.replace(":", ""));
				int fin = Integer.parseInt(changeFinishTime.replace(":", ""));
//				出勤時間を変更していた場合
				if (changeStartTime.length() == 5) {
					start *= 100;
				}
//				退勤時間を変更していた場合
				if (changeFinishTime.length() == 5) {
					fin *= 100;
					changeFinishTime = changeFinishTime + ":00";
				}
				if (start >= fin) {
					mav.addObject("error", "退勤時間が出勤時間よりも早くなっています。");
					mav.setViewName("redirect:/requestForminfo");
					return mav;
				}
				if (comment.equals("")) {
					mav.addObject("error", "変更理由は必ず入力してください。");
					mav.setViewName("redirect:/requestForminfo");
					return mav;
				}
			}

//		入力内容をセット
			Request request = new Request();
			if (changeFinishTime.equals("")) {
				request.setChangeFinishTime(null);
			} else {
				request.setChangeFinishTime(changeFinishTime);
			}
			request.setChangeStartTime(changeStartTime);
			request.setComment(comment);
			request.setAttendanceId(Integer.parseInt(attendanceId));
			request.setEmployeeId(employeeId);
			request.setIsDeleted(false);

//		DBに登録
			requestinfo.saveAndFlush(request);
			mav.addObject("message","変更リクエストを送信しました");
			mav.setViewName("redirect:/employeeMenu");
			return mav;
		} catch (Exception e) {
			mav.setViewName("redirect:/logout");
			mav.addObject("error", "DB接続エラーです。DBを確認してください。");
			return mav;
		}
	}


	/*
	 * 変更履歴確認
	 */
	@RequestMapping("/historicalCheck")
	public ModelAndView historicalCheck(
			@RequestParam(value = "adminId", defaultValue = "", required = false) String adminId,
			@RequestParam(value = "employeeId", defaultValue = "", required = false) String employeeId,
			ModelAndView mav) {
		try {
			//セッション情報の確認
			User user = (User) session.getAttribute("user");
			if (user == null) {
				mav.addObject("error","セッションが切れました。再度ログインしてください。");
				mav.setViewName("login");
				return mav;
			}
			// メッセージを格納する変数
			String message = "";
			// 結果を受け取る変数
			ArrayList<Change> changeList = new ArrayList<Change>();

			// 検索による受け取る情報の分岐
			if (adminId.equals("") && employeeId.equals("")) {// 検索情報がない場合
				changeList = (ArrayList<Change>) changeinfo.findAll();
			} else if (adminId.equals("") && !employeeId.equals("")) {// ユーザーIDでの検索の場合
				message = "社員番号 : " + employeeId + "での検索結果";

				changeList = changeinfo.findByEmployeeIdLike(employeeId + "%");
			} else if (!adminId.equals("") && employeeId.equals("")) {// 管理者IDでの検索の場合
				message = "管理者の社員番号 : " + adminId + "での検索結果";

				changeList = changeinfo.findByAdminIdLike(adminId + "%");
			} else {// ユーザーIDと管理者IDでの検索
				message = "社員番号 : " + employeeId + " 管理者の社員番号 : " + adminId + "での検索結果";

				changeList = changeinfo.findByEmployeeIdAndAdminIdLike(employeeId, adminId);
			}

//		検索結果がなかった時
			if (changeList.size() == 0) {
				mav.addObject("error", "検索結果はありません。");
			}

			// 結果の受け渡し
			mav.addObject("changeList", changeList);
			if (!message.equals("")) {
				mav.addObject("message", message);
			}
			mav.setViewName("historicalCheck");

			return mav;
		} catch (Exception e) {
			mav.setViewName("redirect:/logout");
			mav.addObject("error", "DB接続エラーです。DBを確認してください。");
			return mav;
		}
	}

	/*
	 * ログイン履歴
	 */
	@RequestMapping("/userHistoricalCheck")
	public ModelAndView userHistoricalCheck(
			@RequestParam(value = "employeeId", defaultValue = "", required = false) String employeeId,
			@RequestParam(value = "name", defaultValue = "", required = false) String name,
			@RequestParam(value = "year", defaultValue = "", required = false) String year,
			@RequestParam(value = "month", defaultValue = "", required = false) String month, ModelAndView mav) {
		try {
			//セッション情報の確認
			User sessionUser = (User) session.getAttribute("user");
			if (sessionUser == null) {
				mav.addObject("error","セッションが切れました。再度ログインしてください。");
				mav.setViewName("login");
				return mav;
			}

			// メッセージやエラー文を格納する変数など
			String message = "";
			String error = "";
			int check = 0;

//		検索時の入力内容の確認
			if (!year.equals("") || !month.equals("")) {
				try {
//					年の文字入力をしていないかチェック
					if (!year.equals("")) {
						check = Integer.parseInt(year);
						check = 0;
//						年の桁数チェック
						if (year.length() != 0 && year.length() != 4) {
							check += 1;
						}
					}
//				月の範囲と文字入力のチェック
					if (!month.equals("")) {
						if (Integer.parseInt(month) > 12 || Integer.parseInt(month) < 1) {
							check += 2;
						}
					}
//			検索にエラーがあったらエラー文を分ける分岐
					switch (check) {
					case 0:
						break;
					case 1:
						error = "年は４桁で入力してください";
						break;
					case 2:
						error = "月は１～１２月の間で入力してください";
						break;
					case 3:
						error = "年は４桁で、月は１～１２月の間で入力してください";
						break;
					}
				} catch (NumberFormatException e) {
					error = "検索の年月は半角数字でお願いします";
				} finally {
//					検索の入力内容にエラーがあった場合
					if (!error.equals("")) {
						mav.addObject("error", error);
						mav.addObject("message", message);
						mav.setViewName("userHistoricalCheck");
						return mav;
					}
				}
			}

// 		月が1桁で入力されt場合の処理
			if (month.length() == 1 && month != "") {
				month = "0" + month;
			}

// 		検索する日付を格納する変数
			String day;
			Date date = new Date();

//		渡す年月の形を変換するフォーマット
			SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy年MM月");

			ArrayList<User> userList = new ArrayList<User>();

// 		検索条件での条件分岐
			if (year.equals("") && month.equals("")) {// 検索条件がない場合
				SimpleDateFormat monthData = new SimpleDateFormat("yyyy-MM");
				day = monthData.format(date) + "%";
				mav.addObject("month", monthFormat.format(date));
			} else if (!year.equals("") && !month.equals("")) {// 年と月で検索された場合
				message = year + "年" + month + "月";

				day = year + "-" + month + "%";
				mav.addObject("month", year + "年" + month + "月");
			} else if (year.equals("") && !month.equals("")) {// 月で検索した場合
				message = month + "月";

				SimpleDateFormat yearData = new SimpleDateFormat("yyyy");
				day = yearData.format(date) + "-" + month + "%";
				mav.addObject("month", yearData.format(date) + "年" + month + "月");
			} else {// 年で検索した場合
				message = year + "年";

				day = year + "%";
				mav.addObject("month", year + "年");
			}

			String cmd = "";

			if (!name.equals("") && !employeeId.equals("")) {// 社員番号と名前が入力されている
				message += " 社員番号 : " + employeeId + " 名前 : " + name + "での検索結果";

				userList = userinfo.findByNameLike("%" + name + "%");
				String testEmployeeId = "";
				for (int i = 0; i < userList.size(); i++) {
					if (employeeId.equals(userList.get(i).getEmployeeId())) {
						testEmployeeId = employeeId;
						userList = new ArrayList<User>();
						break;
					}
				}
				employeeId = testEmployeeId;
				userList = new ArrayList<User>();
				if (employeeId.equals("")) {
					cmd = "n";
				}
			} else if (!name.equals("") && employeeId.equals("")) {// 名前のみ
				message += " 名前 : " + name + "での検索結果";

				userList = userinfo.findByNameLike("%" + name + "%");
				if (userList.isEmpty()) {
					cmd = "n";
				}
			} else if (name.equals("") && !employeeId.equals("")) {
				message += "社員番号 : " + employeeId + "での検索結果";
			} else {
				message += "での検索結果";
			}

			ArrayList<LoginLog> loginLogList = new ArrayList<LoginLog>();
			if (userList.isEmpty() && cmd.equals("")) {
				loginLogList = logininfo.findByEmployeeIdLikeAndLoginTimeLike("%" + employeeId + "%", day);
			} else if (!userList.isEmpty() && cmd.equals("")) {
				for (int i = 0; i < userList.size(); i++) {
					ArrayList<LoginLog> testLoginLog = new ArrayList<LoginLog>();
					testLoginLog = logininfo.findByEmployeeIdLikeAndLoginTimeLike(userList.get(i).getEmployeeId(), day);
					for (int j = 0; j < testLoginLog.size(); j++) {
						loginLogList.add(testLoginLog.get(j));
					}
				}
			}

			ArrayList<LoginLogName> nameList = new ArrayList<LoginLogName>();

			String strFormat = "dd日 HH時mm分ss秒";
			if (month.equals("") && !year.equals("")) {
				strFormat = "MM月dd日 HH時mm分ss秒";
			}

			SimpleDateFormat sdf = new SimpleDateFormat(strFormat);
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			Date dayData;

			for (int i = 0; i < loginLogList.size(); i++) {
				LoginLogName loginLogName = new LoginLogName();
				User user = userinfo.findByEmployeeId(loginLogList.get(i).getEmployeeId());

				try {
					dayData = format.parse(loginLogList.get(i).getLoginTime());

//		 送る値を格納する
					loginLogName.setEmployeeId(loginLogList.get(i).getEmployeeId());
					loginLogName.setLoginTime(sdf.format(dayData));
					loginLogName.setName(user.getName());
					nameList.add(loginLogName);
				} catch (ParseException e) {
				}

			}

//		リストが空でなければmavに登録
			if (loginLogList.size() != 0) {
				mav.addObject("loginLogList", nameList);
			} else {
				mav.addObject("error", "検索結果はありません。");
			}

			mav.addObject("message", message);

//		遷移先の指定
			mav.setViewName("userHistoricalCheck");
			return mav;
		} catch (Exception e) {
			mav.setViewName("redirect:/logout");
			mav.addObject("error", "DB接続エラーです。DBを確認してください。");
			return mav;
		}
	}

//以下画面遷移用リクエストマッピング
	@RequestMapping("/loginForm")
	public ModelAndView loginForm(ModelAndView mav) {
		mav.setViewName("login");
		return mav;
	}

	@RequestMapping("/employeeMenu")
	public ModelAndView employeeMenu(ModelAndView mav,
			@RequestParam(value = "message", defaultValue = "") String message) {
		try {
			// セッション情報の確認
			User user = (User) session.getAttribute("user");
			if (user == null) {
				mav.addObject("error", "セッションが切れました。再度ログインしてください。");
				mav.setViewName("login");
				return mav;
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
					if (attendance.getStartEngrave() != null) {
						startEngrave = time.parse(attendance.getStartEngrave());
						mav.addObject("startTime", time.format(startEngrave));
					}
					if (attendance.getFinishEngrave() != null) {
						finishEngrave = time.parse(attendance.getFinishEngrave());
						mav.addObject("finishTime", time.format(finishEngrave));
					}
				} catch (ParseException e) {
				}
			}
			mav.setViewName("employeeMenu");
			mav.addObject("message", message);
			return mav;
		} catch (Exception e) {
			mav.setViewName("redirect:/logout");
			mav.addObject("error", "DB接続エラーです。DBを確認してください。");
			return mav;
		}
	}

	@RequestMapping("/adminMenu")
	public ModelAndView adminMenu(ModelAndView mav) {
		try {
			//セッション情報の確認
			User user = (User) session.getAttribute("user");
			if (user == null) {
				mav.addObject("error","セッションが切れました。再度ログインしてください。");
				mav.setViewName("login");
				return mav;
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
					if (attendance.getStartEngrave() != null) {
						startEngrave = time.parse(attendance.getStartEngrave());
						mav.addObject("startTime", time.format(startEngrave));
					}
					if (attendance.getFinishEngrave() != null) {
						finishEngrave = time.parse(attendance.getFinishEngrave());
						mav.addObject("finishTime", time.format(finishEngrave));
					}
				} catch (ParseException e) {
				}
			}
			mav.setViewName("adminMenu");
			return mav;
		} catch (Exception e) {
			mav.setViewName("redirect:/logout");
			mav.addObject("error", "DB接続エラーです。DBを確認してください。");
			return mav;
		}
	}

	@RequestMapping("/employeeRegistrationinfo")
	public ModelAndView employeeRegistrationinfo(ModelAndView mav) {
		//セッション情報の確認
			User sessionUser = (User) session.getAttribute("user");
			if (sessionUser == null) {
				mav.addObject("error","セッションが切れました。再度ログインしてください。");
				mav.setViewName("login");
				return mav;
			}
		mav.setViewName("employeeRegistration");
		return mav;
	}

	@RequestMapping("/changeAttendanceinfo")
	public ModelAndView changeAttendanceinfo(@RequestParam("employeeId") String employeeId,
			@RequestParam("attendanceId") String attendanceId,
			@RequestParam(value = "error", defaultValue = "") String error, ModelAndView mav) {
		try {
			// セッション情報の確認
			User sessionUser = (User) session.getAttribute("user");
			if (sessionUser == null) {
				mav.addObject("error", "セッションが切れました。再度ログインしてください。");
				mav.setViewName("login");
				return mav;
			}
			User user = userinfo.findByEmployeeId(employeeId);
			Attendance attendance = attendanceinfo.findByAttendanceId(Integer.parseInt(attendanceId));
			mav.addObject(user);
			mav.addObject(attendance);
			if (!error.equals("")) {
				mav.addObject("error", error);
			}
			mav.setViewName("changeAttendance");
			return mav;
		} catch (Exception e) {
			mav.setViewName("redirect:/logout");
			mav.addObject("error", "DB接続エラーです。DBを確認してください。");
			return mav;
		}
	}

//	変更リクエストForm
	@RequestMapping("/requestForminfo")
	public ModelAndView requestForminfo(ModelAndView mav, @ModelAttribute("error") String error) {
		try {
			//セッション情報の確認
			User user = (User) session.getAttribute("user");
			if (user == null) {
				mav.addObject("error","セッションが切れました。再度ログインしてください。");
				mav.setViewName("login");
				return mav;
			}
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String day = sdf.format(date);
			ArrayList<Attendance> list = attendanceinfo.findByDayAndEmployeeId(day, user.getEmployeeId());
			Attendance attendance;
			if (list.size() > 0) {
				attendance = list.get(0);
				mav.addObject(user);
				mav.addObject(attendance);
				if (!error.equals("")) {
					mav.addObject("error", error);
				}
				mav.setViewName("requestForm");
			} else {
//			当日の勤怠登録前には何も起きない
				mav.addObject("error", "本日の打刻を行ってください‼");
				mav.setViewName("employeeMenu");
			}

			return mav;
		} catch (Exception e) {
			mav.setViewName("redirect:/logout");
			mav.addObject("error", "DB接続エラーです。DBを確認してください。");
			return mav;
		}
	}

	/*
	 * 画像を解凍する
	 */
	public String photoView(String employeePhoto) {
		// 画像を検索してbyteとしてViewへ受け渡す
		String uploadPath = "photo/" + employeePhoto;
		// 画像データストリームを取得する
		try (FileInputStream fis = new FileInputStream(uploadPath);) {
			StringBuffer data = new StringBuffer();
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			// バイト配列に変換
			while (true) {
				int len = fis.read(buffer);
				if (len < 0) {
					break;
				}
				os.write(buffer, 0, len);
			}
			// 画像データをbaseにエンコード
			String base64 = new String(org.apache.tomcat.util.codec.binary.Base64.encodeBase64(os.toByteArray()),
					"ASCII");
			// 画像タイプはJPEG
			// Viewへの受け渡し。append("data:~~)としているとtymleafでの表示が楽になる
			data.append("data:image/jpeg;base64,");
			data.append(base64);

			return data.toString();

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
