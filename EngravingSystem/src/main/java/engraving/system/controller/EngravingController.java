package engraving.system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.beans.factory.annotation.Autowired;

import engraving.system.entity.*;
import engraving.system.repository.*;

@Controller
public class EngravingController {

	//Repositoryインターフェースを自動インスタンス化
	@Autowired
	private AttendanceRepository attendanceinfo;
	
	//「engraving」にアクセスがあった場合
	
	
}
