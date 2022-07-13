package engraving.system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;

import engraving.system.entity.*;
import engraving.system.repository.*;

@Controller
public class EngravingController {

	//Repositoryインターフェースを自動インスタンス化
	@Autowired
	private AttendanceRepository attendanceinfo;
	
	//「/startEngraving」にアクセスがあった場合
	@RequestMapping(value = "/startEngraving", method = RequestMethod.POST)
	public ModelAndView engravingStart(@ModelAttribute Attendance attendance, ModelAndView mav) {
		//入力された情報をDBに保存
		attendanceinfo.saveAndFlush(attendance);
		
		//リダイレクト先を指定
		mav = new ModelAndView("redirect;/menu");
		
		//ModelとView情報を返す
		return mav;
	}
	
	//「finishEngraving」にアクセスがあった場合
	@RequestMapping(value = "/finishEngraving", method = RequestMethod.POST)
	public ModelAndView finishEngraving(ArrayList finishEngraving, ModelAndView mav) {
		
		//入力された情報を更新
		
		
		//リダイレクト先を指定
		mav = new ModelAndView("redirect:/menu");
		
		//ModelとView情報を返す
		return mav;
	}
	
}
