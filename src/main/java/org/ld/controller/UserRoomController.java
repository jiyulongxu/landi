package org.ld.controller;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.util.Streams;
import org.apache.log4j.Logger;
import org.ld.app.CurEnv;
import org.ld.model.DailyService;
import org.ld.model.Guest;
import org.ld.model.Laundry;
import org.ld.model.Room;
import org.ld.model.RoomItem;
import org.ld.model.RoomMeter;
import org.ld.model.RoomPic;
import org.ld.model.RoomState;
import org.ld.model.Sources;
import org.ld.service.GuestMissionService;
import org.ld.service.ItemService;
import org.ld.service.RoomService;
import org.ld.service.ServerService;
import org.ld.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;
import com.sun.org.apache.xerces.internal.util.SynchronizedSymbolTable;

@Controller
@RequestMapping("/userRoom")
public class UserRoomController {

	@Autowired
	private UserService userService;
	@Autowired
	private RoomService roomService;
	@Autowired
	private GuestMissionService guestService;
	@Autowired
	private ServerService serverService;
	@Autowired
	private ItemService itemService;

	private static Logger logger = Logger.getLogger("logRec");

	// 多文件上传(add by pq)
	@RequestMapping(value = "/uploadFiles", method = RequestMethod.POST)
	public String uploadFiles(@RequestParam("file") MultipartFile[] file, Integer room_id, HttpServletRequest request) {
		// System.out.println(request.getSession().getServletContext().getRealPath(""));
		System.out.println("room_id：" + room_id);
		String roomNumber = roomService.getRoomById(room_id).getROOM_NUMBER();
		// 遍历文件
		for (MultipartFile mul : file) {
			System.out.println(mul.getName() + "---" + mul.getContentType() + "---" + mul.getOriginalFilename());
			try {
				if (!mul.isEmpty()) {
					Streams.copy(mul.getInputStream(),
							new FileOutputStream(request.getSession().getServletContext().getRealPath("")
									+ "/resources/room_pic/" + mul.getOriginalFilename()),
							true);

					RoomPic roompic = new RoomPic();
					roompic.setROOM_ID(room_id);
					roompic.setTYPE(1);
					;
					roompic.setCTIME(null);
					roompic.setNAME("1");
					roompic.setTAG("1");
					roompic.setPATH("/resources/room_pic/" + mul.getOriginalFilename());

					roomService.insertRoomPic(roompic);
				}
			} catch (IOException e) {
				System.out.println("文件上传失败");
				e.printStackTrace();
			}
		}

		return "forward:/views/user/tenant/roomCheck.jsp?rid=" + room_id + "&rNum=" + roomNumber;
	}

	// 获取房间图片路径(add by pq)
	@RequestMapping(value = "/getRoomPic", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public List<RoomPic> getRoomPic(@RequestParam(value = "id", required = true) Integer room_id) throws Exception {

		System.out.println(room_id);
		List<RoomPic> roomPic = roomService.getPic(room_id);

		return roomPic;
	}

	// 根据roomNumber 查询 roomID(add by pq)
	@RequestMapping(value = "/getRoomIDByNumber")
	@ResponseBody
	public Room getRoomIDByNumber(@RequestParam(value = "roomNumber", required = true) String roomNumber)
			throws Exception {

		System.out.println(roomNumber);
		Room room = roomService.getRoomByNumber(roomNumber);

		return room;
	}

	@RequestMapping("/getAllRoom") // 所有房间
	@ResponseBody
	public Map<String, Object> getAllRoom(HttpSession session) {
		CurEnv cur_env = (CurEnv) session.getAttribute("CUR_ENV");
		Map<String, Object> ans = new HashMap<String, Object>();
		if ((cur_env.getCur_user().getAUTH() & (0x01 << cur_env.getAuths().get("rRoom"))) == 0) {
			ans.put("State", "Invalid");
			return ans;
		} else {
			ans.put("State", "Valid");
		}

		List<Room> rooms = roomService.getAllRoom();
		ans.put("roomList", rooms);

		return ans;
	}

	@RequestMapping("/getAllRoomState") // 获取所有房间状态信息（租客一览表）
	@ResponseBody
	public Map<String, Object> getAllRoomState(HttpSession session) {
		CurEnv cur_env = (CurEnv) session.getAttribute("CUR_ENV");
		Map<String, Object> ans = new HashMap<String, Object>();
		if ((cur_env.getCur_user().getAUTH() & (0x01 << cur_env.getAuths().get("rRoom"))) == 0) {
			ans.put("State", "Invalid");
			return ans;
		} else {
			ans.put("State", "Valid");
		}

		List<RoomState> rooms = roomService.getAllRoomState();
		ans.put("roomStateList", rooms);

		return ans;
	}

	@RequestMapping("/getRoomInfo") // 获取房间详细信息
	@ResponseBody
	public Map<String, Object> getOneRoom(HttpSession session, @RequestBody String data) {
		JSONObject dataJson = JSONObject.parseObject(data);

		String op = dataJson.getString("op");
		int rid = dataJson.getIntValue("rid");
		String rn = dataJson.getString("rNum");

		CurEnv cur_env = (CurEnv) session.getAttribute("CUR_ENV");
		Map<String, Object> ans = new HashMap<String, Object>();

		if ((cur_env.getCur_user().getAUTH() & (0x01 << cur_env.getAuths().get("rRoom"))) == 0) {
			ans.put("State", "Invalid");
			return ans;
		} else {
			ans.put("State", "Valid");
		}

		if(op.equals("room")) {
			Room room = roomService.getRoomById(rid);
			ans.put("room", room);
		} else if(op.equals("guest")) {
			Guest guest = guestService.getGuestByRoomNumber(rn);
			ans.put("guest_info", guest);
		} else {
			String type = dataJson.getString("type");
			int pageNumber = dataJson.getIntValue("pageNum");

			int eachPage = cur_env.getSettingsInt().get("list_size");
			int recordTotal = itemService.totalItemByRoomType(rid, type);
			int pageTotal = (int) Math.ceil((float) recordTotal / eachPage);

			if (recordTotal != 0) {
				if (pageNumber > pageTotal)
					pageNumber = pageTotal;

				int st = (pageNumber - 1) * eachPage;
				List<RoomItem> record = roomService.getItems(rid, op, st, eachPage);

				ans.put("pageList", record);
			}

			ans.put("pageNow", pageNumber);
			ans.put("pageTotal", pageTotal);
			ans.put("recordTotal", recordTotal);
		}

		return ans;
	}

	@RequestMapping("/getPics")
	@ResponseBody
	public Map<String, Object> getPics(HttpSession session, @RequestBody Integer rid) {
		CurEnv cur_env = (CurEnv) session.getAttribute("CUR_ENV");
		Map<String, Object> ans = new HashMap<String, Object>();
		if ((cur_env.getCur_user().getAUTH() & (0x01 << cur_env.getAuths().get("rRoom"))) == 0) {
			ans.put("State", "Invalid");
			return ans;
		} else {
			ans.put("State", "Valid");
		}

		List<RoomPic> pic = roomService.getPic(rid);
		ans.put("pics", pic);
		return ans;
	}

	@RequestMapping("/getMeters") // 查meter（一行）
	@ResponseBody
	public Map<String, Object> getMeters(HttpSession session, Integer rid, Integer type) {
		CurEnv cur_env = (CurEnv) session.getAttribute("CUR_ENV");
		Map<String, Object> ans = new HashMap<String, Object>();
		if ((cur_env.getCur_user().getAUTH() & (0x01 << cur_env.getAuths().get("rRoom"))) == 0) {
			ans.put("State", "Invalid");
			return ans;
		} else {
			ans.put("State", "Valid");
		}

		List<RoomMeter> meters = roomService.getMeters(rid, type);
		ans.put("meters" + type, meters);
		return ans;
	}

	@RequestMapping("/roomSearchBill") // 明细流水（客房服务）
	@ResponseBody
	public Map<String, Object> searchBill(HttpSession session, @RequestBody String data) {
		CurEnv cur_env = (CurEnv) session.getAttribute("CUR_ENV");
		Map<String, Object> ans = new HashMap<String, Object>();
		if ((cur_env.getCur_user().getAUTH() & (0x01 << cur_env.getAuths().get("rDaily"))) == 0) {
			ans.put("State", "Invalid");
			return ans;
		} else {
			ans.put("State", "Valid");
		}

		JSONObject dataJson = JSONObject.parseObject(data);

		int type = dataJson.getIntValue("type");
		int pageNumber = dataJson.getIntValue("pageNum");
		String rn = dataJson.getString("rNum");

		int eachPage = cur_env.getSettingsInt().get("list_size");
		int recordTotal = serverService.getTotalDailyServiceRow(rn, type);
		int pageTotal = (int) Math.ceil((float) recordTotal / eachPage);

		if (recordTotal != 0) {
			if (pageNumber > pageTotal)
				pageNumber = pageTotal;

			int st = (pageNumber - 1) * eachPage;
			List<DailyService> record = serverService.searchBill(rn, type, st, eachPage);

			ans.put("pageList", record);
		}

		ans.put("pageNow", pageNumber);
		ans.put("pageTotal", pageTotal);
		ans.put("recordTotal", recordTotal);

		return ans;
	}

	@RequestMapping("/roomSearchSource") // 查 sources
	@ResponseBody
	public Map<String, Object> searchSourch(HttpSession session, @RequestBody String data) {
		CurEnv cur_env = (CurEnv) session.getAttribute("CUR_ENV");
		Map<String, Object> ans = new HashMap<String, Object>();
		if ((cur_env.getCur_user().getAUTH() & (0x01 << cur_env.getAuths().get("rDaily"))) == 0) {
			ans.put("State", "Invalid");
			return ans;
		} else {
			ans.put("State", "Valid");
		}

		JSONObject dataJson = JSONObject.parseObject(data);

		// 1 water 2 power 3 gas
		int type = dataJson.getIntValue("type");
		int pageNumber = dataJson.getIntValue("pageNum");
		String rn = dataJson.getString("rNum");

		int eachPage = cur_env.getSettingsInt().get("list_size");
		int recordTotal = serverService.getTotalSourcesRow(rn, type);
		int pageTotal = (int) Math.ceil((float) recordTotal / eachPage);

		if (recordTotal != 0) {
			if (pageNumber > pageTotal)
				pageNumber = pageTotal;

			int st = (pageNumber - 1) * eachPage;
			List<Sources> record = serverService.searchSource(rn, type, st, eachPage);

			ans.put("pageList", record);
		}

		ans.put("pageNow", pageNumber);
		ans.put("pageTotal", pageTotal);
		ans.put("recordTotal", recordTotal);

		return ans;
	}

	@RequestMapping("/addService") // 添加客房服务
	@ResponseBody
	public Integer addService(HttpSession session, @RequestBody String data) {
		CurEnv cur_env = (CurEnv) session.getAttribute("CUR_ENV");
		if ((cur_env.getCur_user().getAUTH() & (0x01 << cur_env.getAuths().get("wDaily"))) == 0) {
			return 0;
		}
		try {
			JSONObject dataJson = JSONObject.parseObject(data);
			DailyService newDS = new DailyService();
			newDS.setCOUNT(dataJson.getInteger("count"));
			newDS.setGUEST_NAME(dataJson.getString("guestName"));
			newDS.setITEM(dataJson.getString("item"));
			newDS.setCOMMENT(dataJson.getString("note"));
			newDS.setROOM_NUMBER(dataJson.getString("roomNumber"));
			newDS.setMONEY(dataJson.getDouble("sum"));
			newDS.setTYPE(dataJson.getInteger("type"));
			SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date;
			date = ft.parse(dataJson.getString("delivery"));
			newDS.setRTIME(date);

			return serverService.addDailyService(newDS);
		} catch (ParseException e) {
			e.printStackTrace();
			return 0;
		}
	}

	@RequestMapping("/addSource") // 添加水费电费
	@ResponseBody
	public Integer addSource(HttpSession session, @RequestBody String data) {
		CurEnv cur_env = (CurEnv) session.getAttribute("CUR_ENV");
		if ((cur_env.getCur_user().getAUTH() & (0x01 << cur_env.getAuths().get("wDaily"))) == 0) {
			return 0;
		}
		try {
			JSONObject dataJson = JSONObject.parseObject(data);
			RoomMeter meter = roomService.getMeter(dataJson.getString("meterNo"));
			Sources newSrc = new Sources();
			newSrc.setROOM_NUMBER(dataJson.getString("roomNumber"));
			newSrc.setGUEST_NAME(dataJson.getString("guestName"));
			newSrc.setCURRENT_DATA(dataJson.getDouble("thisMonthNum"));
			newSrc.setMONEY(dataJson.getDouble("charge"));
			newSrc.setTYPE(dataJson.getInteger("type"));
			newSrc.setMETER(dataJson.getString("meterNo"));
			newSrc.setLAST_DATA(meter.getCUR_VAL());
			newSrc.setCOUNT(newSrc.getCURRENT_DATA()-newSrc.getLAST_DATA());

			SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date;
			date = ft.parse(dataJson.getString("meterDate"));
			newSrc.setTIME(date);

			 meter.setLAST_MONTH_VAL(meter.getCUR_VAL());
			 meter.setCUR_VAL(newSrc.getCURRENT_DATA());
			 meter.setCUR_TIME(newSrc.getTIME());

			if (serverService.addSources(newSrc) == 1) {
				 return roomService.updateMeter(meter);
			} else {
				return 0;
			}
		} catch (ParseException e) {
			e.printStackTrace();
			return 0;
		}
	}

	@RequestMapping("/addSourceGas") // 添加燃气费
	@ResponseBody
	public Integer addSourceGas(HttpSession session, @RequestBody String data) {
		CurEnv cur_env = (CurEnv) session.getAttribute("CUR_ENV");
		if ((cur_env.getCur_user().getAUTH() & (0x01 << cur_env.getAuths().get("wDaily"))) == 0) {
			return 0;
		}
		try {
			JSONObject dataJson = JSONObject.parseObject(data);
			RoomMeter meter = roomService.getMeter(dataJson.getString("firstMeterNo"));
			Sources newSrc = new Sources();
			newSrc.setROOM_NUMBER(dataJson.getString("roomNumber"));
			newSrc.setGUEST_NAME(dataJson.getString("guestName"));
			newSrc.setCURRENT_DATA(dataJson.getDouble("firstthisMonthNum"));
			newSrc.setMONEY(dataJson.getDouble("firstCharge"));
			newSrc.setTYPE((Integer) cur_env.getSettingsInt().get("source_gas"));
			newSrc.setMETER(dataJson.getString("firstMeterNo"));
			newSrc.setLAST_DATA(meter.getCUR_VAL());
			newSrc.setCOUNT(newSrc.getCURRENT_DATA() - newSrc.getLAST_DATA());

			SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date;
			date = ft.parse(dataJson.getString("meterDate"));
			newSrc.setTIME(date);

			meter.setLAST_MONTH_VAL(meter.getCUR_VAL());
			meter.setCUR_VAL(newSrc.getCURRENT_DATA());
			meter.setCUR_TIME(newSrc.getTIME());

			if (serverService.addSources(newSrc) == 1) {
				roomService.updateMeter(meter);
			} else {
				return 0;
			}

			meter = roomService.getMeter(dataJson.getString("secondMeterNo"));
			newSrc.setCURRENT_DATA(dataJson.getDouble("secondthisMonthNum"));
			newSrc.setMONEY(dataJson.getDouble("secondCharge"));
			newSrc.setTYPE((Integer) cur_env.getSettingsInt().get("source_gas"));
			newSrc.setMETER(dataJson.getString("secondMeterNo"));
			newSrc.setLAST_DATA(meter.getCUR_VAL());
			newSrc.setCOUNT(newSrc.getCURRENT_DATA() - newSrc.getLAST_DATA());

			meter.setLAST_MONTH_VAL(meter.getCUR_VAL());
			meter.setCUR_VAL(newSrc.getCURRENT_DATA());
			meter.setCUR_TIME(newSrc.getTIME());

			if (serverService.addSources(newSrc) == 1) {
				return roomService.updateMeter(meter);
			} else {
				return 0;
			}
		} catch (ParseException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	@RequestMapping("/searchWash") // roomNum为null时，查询所有记录
	@ResponseBody
	public Map<String, Object> searchWash(HttpSession session, @RequestBody String data) {
		JSONObject dataJson = JSONObject.parseObject(data);

		CurEnv cur_env = (CurEnv) session.getAttribute("CUR_ENV");
		Map<String, Object> ans = new HashMap<String, Object>();

		if ((cur_env.getCur_user().getAUTH() & (0x01 << cur_env.getAuths().get("rRoom"))) == 0) {
			ans.put("State", "Invalid");
			return ans;
		} else {
			ans.put("State", "Valid");
		}

		int pageNumber = dataJson.getIntValue("pageNum");
		String roomNum = dataJson.getString("roomNum");
		int eachPage = cur_env.getSettingsInt().get("list_size");
		int recordTotal = roomService.totalLaundry(roomNum);
		int pageTotal = (int) Math.ceil((float) recordTotal / eachPage);

		if (recordTotal != 0) {
			if (pageNumber > pageTotal)
				pageNumber = pageTotal;

			int st = (pageNumber - 1) * eachPage;
			List<Laundry> record = roomService.getLaundry(roomNum, st, eachPage);

			ans.put("dataList", record);
		}

		ans.put("pageNow", pageNumber);
		ans.put("pageTotal", pageTotal);
		ans.put("recordTotal", recordTotal);

		return ans;
	}

	@RequestMapping("/addWash") // roomNum为null时，查询所有记录
	@ResponseBody
	public Integer addWash(HttpSession session,  @RequestBody String data) {
		JSONObject dataJson = JSONObject.parseObject(data);
		
		CurEnv cur_env = (CurEnv) session.getAttribute("CUR_ENV");

		if ((cur_env.getCur_user().getAUTH() & (0x01 << cur_env.getAuths().get("wRoom"))) == 0) {
			return 0;
		}
		
		try{
			SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
			Date date;
			date = ft.parse(dataJson.getString("date"));
		
			Laundry nL = roomService.getCertainLaundry(dataJson.getString("roomNum"), dataJson.getString("guestName"), date);
			if(nL == null) {
				nL = new Laundry();
				nL.setROOM_NUM(dataJson.getString("roomNum"));
				nL.setNAME(dataJson.getString("guestName"));
				nL.setDATE(date);
				nL.setPRICE(dataJson.getDoubleValue("totalPrice"));
				nL.setTOTAL(dataJson.getInteger("total"));
				
				nL.setCASHMERE(dataJson.getInteger("cashmere"));
				nL.setCOAT_L(dataJson.getInteger("longCoat"));
				nL.setCOAT_M(dataJson.getInteger("middleCoat"));
				nL.setCOAT_ML(dataJson.getInteger("longCotton"));
				nL.setCOAT_MM(dataJson.getInteger("middleCotton"));
				nL.setCOAT_MS(dataJson.getInteger("shortCotton"));
				nL.setJACKET(dataJson.getInteger("jacket"));
				nL.setKNITTED(dataJson.getInteger("knitted"));
				nL.setLONG_SKIRT(dataJson.getInteger("longSkirt"));
				nL.setOTHER(dataJson.getInteger("other"));
				nL.setSHIRT(dataJson.getInteger("shirt"));
				nL.setSHORT_PANTS(dataJson.getInteger("shortPants"));
				nL.setSHORT_SKIRT(dataJson.getInteger("shortSkirt"));
				nL.setT_SHRIT(dataJson.getInteger("tshirt"));
				nL.setTIE(dataJson.getInteger("tie"));
				nL.setTOP_OF_SUIT(dataJson.getInteger("topSuit"));
				nL.setTROUSERS(dataJson.getInteger("trousers"));
				nL.setWAISTCOAT(dataJson.getInteger("waistcoat"));
				
				roomService.addWash(nL);
			}
			else
			{
				nL.setPRICE(nL.getPRICE() + dataJson.getDoubleValue("totalPrice"));
				nL.setTOTAL(nL.getTOTAL() + dataJson.getInteger("total"));
				
				nL.setCASHMERE(nL.getCASHMERE() + dataJson.getInteger("cashmere"));
				nL.setCOAT_L(nL.getCOAT_L() + dataJson.getInteger("longCoat"));
				nL.setCOAT_M(nL.getCOAT_M() + dataJson.getInteger("middleCoat"));
				nL.setCOAT_ML(nL.getCOAT_ML() + dataJson.getInteger("longCotton"));
				nL.setCOAT_MM(nL.getCOAT_MM() + dataJson.getInteger("middleCotton"));
				nL.setCOAT_MS(nL.getCOAT_MS() + dataJson.getInteger("shortCotton"));
				nL.setJACKET(nL.getJACKET() + dataJson.getInteger("jacket"));
				nL.setKNITTED(nL.getKNITTED() + dataJson.getInteger("knitted"));
				nL.setLONG_SKIRT(nL.getLONG_SKIRT() + dataJson.getInteger("longSkirt"));
				nL.setOTHER(nL.getOTHER() + dataJson.getInteger("other"));
				nL.setSHIRT(nL.getSHIRT() + dataJson.getInteger("shirt"));
				nL.setSHORT_PANTS(nL.getSHORT_PANTS() + dataJson.getInteger("shortPants"));
				nL.setSHORT_SKIRT(nL.getSHORT_SKIRT() + dataJson.getInteger("shortSkirt"));
				nL.setT_SHRIT(nL.getT_SHRIT() + dataJson.getInteger("tshirt"));
				nL.setTIE(nL.getTIE() + dataJson.getInteger("tie"));
				nL.setTOP_OF_SUIT(nL.getTOP_OF_SUIT() + dataJson.getInteger("topSuit"));
				nL.setTROUSERS(nL.getTROUSERS() + dataJson.getInteger("trousers"));
				nL.setWAISTCOAT(nL.getWAISTCOAT() + dataJson.getInteger("waistcoat"));
				
				roomService.updateWash(nL);
			}
	
			return 1;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
	}

	@RequestMapping("/Model/")
	@ResponseBody
	public Map<String, Object> Model(HttpSession session, @RequestBody Integer rid) {
		CurEnv cur_env = (CurEnv) session.getAttribute("CUR_ENV");
		Map<String, Object> ans = new HashMap<String, Object>();
		if ((cur_env.getCur_user().getAUTH() & (0x01 << cur_env.getAuths().get("rRoom"))) == 0) {
			ans.put("State", "Invalid");
			return ans;
		} else {
			ans.put("State", "Valid");
		}

		return ans;
	}
}
