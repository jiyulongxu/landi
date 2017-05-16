package org.ld.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.ld.app.Config;
import org.ld.dao.FlightPickingMapper;
import org.ld.dao.LaundryMapper;
import org.ld.dao.MaintainMapper;
import org.ld.dao.RoomItemMapper;
import org.ld.dao.RoomMapper;
import org.ld.dao.RoomMeterMapper;
import org.ld.dao.RoomPicMapper;
import org.ld.dao.RoomStateMapper;
import org.ld.dao.ShuttleBusMapper;
import org.ld.model.FlightPicking;
import org.ld.model.Laundry;
import org.ld.model.Maintain;
import org.ld.model.Room;
import org.ld.model.RoomItem;
import org.ld.model.RoomMeter;
import org.ld.model.RoomPic;
import org.ld.model.RoomState;
import org.ld.model.ShuttleBus;
import org.ld.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/* 用户service实现类  */
@Service("roomService")
public class RoomServiceImpl implements RoomService {

	private static Logger logger = Logger.getLogger("logDev");

	@Autowired
	private RoomMapper roomMapper;
	@Autowired
	private RoomMeterMapper roomMeterMapper;
	@Autowired
	private RoomItemMapper roomItemMapper;
	@Autowired
	private RoomPicMapper roomPicMapper;
	@Autowired
	private RoomStateMapper roomStateMapper;
	@Autowired
	private LaundryMapper laundryMapper;
	@Autowired
	private ShuttleBusMapper shuttleBusMapper;
	@Autowired
	private MaintainMapper maintainMapper;
	@Autowired
	private FlightPickingMapper flightPickingMapper;

	@Override
	public Room getRoomById(int id) {
		// TODO Auto-generated method stub
		return roomMapper.selectByPrimaryKey(id);
	}

	@Override
	public Room getRoomByNumber(String rn) {
		// TODO Auto-generated method stub
		return roomMapper.selectByNumber(rn);
	}

	@Override
	public List<Room> getRoomByType(String type) {
		// TODO Auto-generated method stub
		return roomMapper.getRoomByType(type);
	}

	@Override
	public List<Room> getRoomByState(int state) {
		// TODO Auto-generated method stub
		return roomMapper.getRoomByState(state);
	}

	@Override
	public int insert(Room roomInfo) {
		// TODO Auto-generated method stub
		try {
			roomMapper.insert(roomInfo);
			return 1;
		} catch (Exception e) {
			logger.error(e.getCause());
			return 0;
		}
	}

	@Override
	public int update(Room roomInfo) {
		// TODO Auto-generated method stub
		try {
			roomMapper.updateByPrimaryKeySelective(roomInfo);
			return 1;
		} catch (Exception e) {
			logger.error(e.getCause());
			return 0;
		}
	}

	@Override
	public int updateMeter(RoomMeter meterInfo) {
		// TODO Auto-generated method stub
		try {
			roomMeterMapper.updateByPrimaryKeySelective(meterInfo);
			return 1;
		} catch (Exception e) {
			logger.error(e.getCause());
			return 0;
		}
	}

	@Override
	public int totalRow() {
		// TODO Auto-generated method stub
		return roomMapper.totalRow();
	}

	@Override
	public List<Room> getAllRoom() {
		// TODO Auto-generated method stub
		return roomMapper.getAllRoom();
	}

	@Override
	public List<RoomState> getAllRoomState() {
		// TODO Auto-generated method stub
		return roomStateMapper.getAllRoomState();
	}

	@Override
	public List<RoomItem> getItems(Integer rid, String type, Integer st, Integer eachPage) {
		// TODO Auto-generated method stub
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("ROOM_ID", rid);
		map.put("TYPE", type);
		map.put("ST", st);
		map.put("EACH", eachPage);
		return roomItemMapper.getItems(map);
	}

	@Override
	public List<RoomPic> getPic(Integer rid) {
		// TODO Auto-generated method stub
		return roomPicMapper.getPicByRoomId(rid);
	}

	// add by pq
	@Override
	public int insertRoomPic(RoomPic roomPic) {
		return roomPicMapper.insertSelective(roomPic);
	}

	@Override
	public List<RoomMeter> getMeters(Integer rid, Integer type) {
		// TODO Auto-generated method stub
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		map.put("ROOM_ID", rid);
		map.put("TYPE", type);
		return roomMeterMapper.getMeters(map);
	}

	@Override
	public RoomMeter getMeter(String mn) {
		// TODO Auto-generated method stub
		return roomMeterMapper.getMeter(mn);
	}

	@Override
	public int totalRowByItem(Integer item_id) {
		// TODO Auto-generated method stub
		return roomItemMapper.getTotalByItemID(item_id);
	}

	@Override
	public List<RoomItem> getItemByItemID(Integer item_id, Integer st, Integer eachPage) {
		// TODO Auto-generated method stub
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("ITEM_ID", item_id);
		map.put("ST", st);
		map.put("EACH", eachPage);
		return roomItemMapper.getItemsByItemID(map);
	}

	@Override
	public RoomItem getCertainRIRec(Integer id) {
		// TODO Auto-generated method stub
		return roomItemMapper.selectByPrimaryKey(id);
	}

	@Override
	public int insertRI(RoomItem ri) {
		// TODO Auto-generated method stub
		try {
			roomItemMapper.insert(ri);
			return 1;
		} catch (Exception e) {
			logger.error(e.getCause());
			return 0;
		}
	}

	@Override
	public int deleteRI(Integer id) {
		// TODO Auto-generated method stub
		try {
			roomItemMapper.deleteByPrimaryKey(id);
			return 1;
		} catch (Exception e) {
			logger.error(e.getCause());
			return 0;
		}
	}
	
	@Override
	public int totalLaundry(String rn, Date date) {
		// TODO Auto-generated method stub
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("RN", rn);
		map.put("DATE", date);
		return laundryMapper.totalRec(map);
	}

	@Override
	public List<Laundry> getLaundry(String rn, Date date, Integer st, Integer eachPage) {
		// TODO Auto-generated method stub
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("RN", rn);
		map.put("DATE", date);
		map.put("ST", st);
		map.put("EACH", eachPage);
		
		return laundryMapper.getRec(map);
	}
	
	@Override
	public List<Laundry> getAllWashes(String rn, Date date) {
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("RN", rn);
		map.put("DATE", date);
		return laundryMapper.getAll(map);
	}

	@Override
	public int addWash(Laundry l) {
		// TODO Auto-generated method stub
		
		try{
			laundryMapper.insertSelective(l);
			return 1;
		} catch (Exception e) {
			logger.error(e.getCause());
			return 0;
		}
	}

	@Override
	public int updateWash(Laundry l) {
		// TODO Auto-generated method stub
		try{
			laundryMapper.updateByPrimaryKeySelective(l);
			return 1;
		} catch (Exception e) {
			logger.error(e.getCause());
			return 0;
		}
	}

	@Override
	public Laundry getCertainLaundry(String rn, Integer gid, Date date) {
		// TODO Auto-generated method stub
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("RN", rn);
		map.put("GID", gid);
		map.put("DATE", date);
		
		return laundryMapper.getCertainRec(map);
	}

	@Override
	public int deleteWash(Integer id) {
	
		try {
			return laundryMapper.deleteByPrimaryKey(id);
		} catch (Exception e) {
			// TODO: handle exception
			logger.error(e.getCause());
			return 0;
		}
	}
	
	@Override
	public Laundry getWashById(Integer id) {
		// TODO Auto-generated method stub
		return laundryMapper.selectByPrimaryKey(id);
	}
	
	@Override
	public int totalShuttleBus(String rn, Integer year, Integer mon) {
		// TODO Auto-generated method stub
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("RN", rn);
		map.put("YEAR", year);
		map.put("MON", mon);
		
		return shuttleBusMapper.totalRec(map);
	}
	
	@Override
	public List<ShuttleBus> getAllShuttleBus(String rn, Integer year, Integer mon) {
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("RN", rn);
		map.put("YEAR", year);
		map.put("MON", mon);
		return shuttleBusMapper.getAllRec(map);
	}

	@Override
	public List<ShuttleBus> getShuttleBus(String rn, Integer year, Integer mon, Integer st, Integer eachPage) {
		// TODO Auto-generated method stub
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("RN", rn);
		map.put("YEAR", year);
		map.put("MON", mon);
		map.put("ST", st);
		map.put("EACH", eachPage);
		
		return shuttleBusMapper.getRec(map);
	}

	@Override
	public ShuttleBus getCertainShuttleBus(String rn, Integer gid, Integer year, Integer mon) {
		// TODO Auto-generated method stub
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("RN", rn);
		map.put("YEAR", year);
		map.put("MON", mon);
		map.put("GID", gid);
		
		return shuttleBusMapper.getCertainRec(map);
	}

	@Override
	public int addShuttleBus(ShuttleBus sb) {
		// TODO Auto-generated method stub
		try{
			shuttleBusMapper.insertSelective(sb);
			return 1;
		} catch (Exception e) {
			logger.error(e.getCause());
			return 0;
		}
	}

	@Override
	public int updateShuttleBus(ShuttleBus sb) {
		// TODO Auto-generated method stub
		try{
			shuttleBusMapper.updateByPrimaryKeySelective(sb);
			return 1;
		} catch (Exception e) {
			logger.error(e.getCause());
			return 0;
		}
	}


	@Override
	public ShuttleBus getShuttleBusById(Integer id) {

		return shuttleBusMapper.selectByPrimaryKey(id);
	}

	@Override
	public int deleteShuttleBus(Integer id) {
		try{
			return shuttleBusMapper.deleteByPrimaryKey(id);
		} catch (Exception e) {
			logger.error(e.getCause());
			return 0;
		}
	}
	
	@Override
	public int getFareUnitPrice(String roomNum) {
		// TODO Auto-generated method stub
		String floor = "车费_" + roomNum.substring(0, 1) + "-" + roomNum.substring(1, roomNum.indexOf('-'));
		return Config.charge.get(floor);
	}
	
	@Override
	public int addMaintain(Maintain m) {
		// TODO Auto-generated method stub
		try{
			maintainMapper.insertSelective(m);
			return 1;
		} catch (Exception e) {
			logger.error(e.getCause());
			return 0;
		}
	}

	@Override
	public int updateMaintain(Maintain m) {
		// TODO Auto-generated method stub
		try{
			maintainMapper.updateByPrimaryKeySelective(m);
			return 1;
		} catch (Exception e) {
			logger.error(e.getCause());
			return 0;
		}
	}

	@Override
	public int totalMaintain(Integer type, Integer cat, Integer state, String rn, Date from, Date to) {
		// TODO Auto-generated method stub
		HashMap<String, Object> map = new HashMap<String, Object>();
		if(rn != null) map.put("RN", rn);
		if(from != null) map.put("STIME", from);
		if(to != null) map.put("TTIME", to);
		if(type != null) map.put("TYPE", type);
		if(cat != null) map.put("CAT", cat);
		if(state != null) map.put("STATE", state);
		
		return maintainMapper.totalRec(map);
	}

	@Override
	public List<Maintain> getMaintain(Integer type, Integer cat, Integer state, String rn, Integer st, Integer eachPage, Date from,
			Date to, Integer order) {
		// TODO Auto-generated method stub
		HashMap<String, Object> map = new HashMap<String, Object>();
		if(rn != null) map.put("RN", rn);
		if(from != null) map.put("STIME", from);
		if(to != null) map.put("TTIME", to);
		if(type != null) map.put("TYPE", type);
		if(cat != null) map.put("CAT", cat);
		if(state != null) map.put("STATE", state);
		if(order != null)map.put("ORDER", order);
		
		return maintainMapper.getRec(map);
	}

	@Override
	public Maintain getCertainMaintain(int ID) {
		// TODO Auto-generated method stub
		return maintainMapper.selectByPrimaryKey(ID);
	}

	@Override
	public int updateRoomState(RoomState rs) {
		// TODO Auto-generated method stub
		try{
			roomStateMapper.updateByPrimaryKeySelective(rs);
			return 1;
		} catch (Exception e) {
			logger.error(e.getCause());
			return 0;
		}
	}

	@Override
	public RoomState getCertainRSbyID(Integer id) {
		// TODO Auto-generated method stub
		return roomStateMapper.getCertainRoomStateByID(id);
	}

	@Override
	public RoomState getCertainRSbyNumber(String number) {
		// TODO Auto-generated method stub
		return roomStateMapper.getCertainRoomStateByNumber(number);
	}

	/**
	 * 接送机
	 */
	
	@Override
	public int addFlightPicking(FlightPicking bean) {
		// TODO Auto-generated method stub
		try {
			flightPickingMapper.insert(bean);
			return 1;
		}catch(Exception e){
			logger.error(e.getCause());
			return 0;
		}
	}

	@Override
	public int getTotalFlightPickingByRoomNumber_Time(String roomNumber, Date time) {
		// TODO Auto-generated method stub
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("ROOM_NUMBER", roomNumber);
		map.put("TIME", time);
		return flightPickingMapper.getTotalByRoomNumber_Time(map);
	}

	@Override
	public List<FlightPicking> getFlightPickingByRoomNumber_Time(String roomNumber, Date time, int startPage, int eachPage) {
		// TODO Auto-generated method stub
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("ROOM_NUMBER", roomNumber);
		map.put("TIME", time);
		map.put("START_PAGE", startPage);
		map.put("EACH_PAGE", eachPage);
		return flightPickingMapper.selectByRoomNumber_Time(map);
	}

	@Override
	public FlightPicking getFlightPickingById(Integer id) {
		
		return flightPickingMapper.selectByPrimaryKey(id);
	}

	@Override
	public int deleteFlightPickingById(Integer id) {
		// TODO Auto-generated method stub
		try {
			return flightPickingMapper.deleteByPrimaryKey(id);
		}catch(Exception e){
			logger.error(e.getCause());
			return 0;
		}
	}

	@Override
	public int updateFlightPicking(FlightPicking fp) {
		try{
			return flightPickingMapper.updateByPrimaryKeySelective(fp);
		}catch(Exception e){
			logger.error(e.getCause());
			return 0;
		}
	}

	@Override
	public List<FlightPicking> getAllFlightPickings(String roomNumber, Date time) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("ROOM_NUMBER", roomNumber);
		map.put("TIME", time);
		return flightPickingMapper.getAll(map);
	}
}