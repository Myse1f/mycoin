/**
 * Created By Yufan Wu
 * 2019/4/19
 */
package restapi;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import core.*;
import exception.BlockPersistenceException;
import io.swagger.annotations.ApiOperation;
import net.PeerAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

@RestController
@RequestMapping("/mycoin/api")
public class RestfulController {
    private static final Logger logger = LoggerFactory.getLogger(RestfulController.class);

    private final BlockChain chain;

    private final PeerGroup network;

    private final Miner miner;

    @Autowired
    public RestfulController(BlockChain chain, PeerGroup network, Miner miner) {
        this.chain = chain;
        this.network = network;
        this.miner = miner;
    }

    @GetMapping("/block/{hash}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "获取区块信息", notes = "根据hash获取区块信息")
    public Result getBlock(@PathVariable("hash") String hash) {
        Result result = new Result();
        try {
            StoredBlock block = chain.getBlockPersistence().get(new SHA256Hash(hash));
            if (block != null) {
                JSONObject data = Utils.storedBlock2Json(block, false);
                result.setCode(Result.ResultCode.SUCCESS.getCode());
                result.setMessage(Result.ResultCode.SUCCESS.getErrMsg());
                result.setData(data);
            } else {
                result.setCode(Result.ResultCode.BLCOK_NOT_FOUND.getCode());
                result.setMessage(Result.ResultCode.BLCOK_NOT_FOUND.getErrMsg());
            }
        } catch (BlockPersistenceException e) {
            logger.error("Database has some problem when request for block {}" , hash);
            result.setCode(Result.ResultCode.EXCEPTION.getCode());
            result.setMessage(Result.ResultCode.EXCEPTION.getErrMsg());
        }

        return result;
    }

    @GetMapping("/recentblocks")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "获取最近的区块", notes = "获取最近的至多10个区块信息")
    public Result getRecentBlocks() {
        Result result = new Result();
        JSONArray jsonArray = new JSONArray();
        try {
            StoredBlock cursor = chain.getChainTip();
            for (int i = 0; i < 10 && cursor != null; i++) {
                JSONObject jsonObject = Utils.storedBlock2Json(cursor, false);
                jsonArray.add(jsonObject);
                cursor = cursor.getPreviousBlock(chain.getBlockPersistence());
            }
            result.setCode(Result.ResultCode.SUCCESS.getCode());
            result.setMessage(Result.ResultCode.SUCCESS.getErrMsg());
            result.setData(jsonArray);
        } catch (BlockPersistenceException e) {
            logger.error("Database has some problem when request for recent blocks. {}", e);
            result.setCode(Result.ResultCode.EXCEPTION.getCode());
            result.setMessage(Result.ResultCode.EXCEPTION.getErrMsg());
        }

        return result;
    }

    @GetMapping("/allblocks")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "获得所有的区块信息", notes = "区块信息只包含高度和哈希值")
    public Result getAllBlocks() {
        Result result = new Result();
        JSONArray data = new JSONArray();

        try {

            for (StoredBlock cursor = chain.getChainTip(); cursor != null; cursor = cursor.getPreviousBlock(chain.getBlockPersistence())) {
                JSONObject jsonObject = Utils.storedBlock2Json(cursor, true);
                data.add(jsonObject);
            }
            result.setCode(Result.ResultCode.SUCCESS.getCode());
            result.setMessage(Result.ResultCode.SUCCESS.getErrMsg());
            result.setData(data);
        } catch (BlockPersistenceException e) {
            logger.error("Database has some problem when request for recent blocks. {}", e);
            result.setCode(Result.ResultCode.EXCEPTION.getCode());
            result.setMessage(Result.ResultCode.EXCEPTION.getErrMsg());
        }

        return result;
    }

    @GetMapping("/peers")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "获取临接节点", notes = "获取所有临接节点")
    public Result getPeers() {
        Result result = new Result();
        JSONArray jsonArray = new JSONArray();
        List<Peer> peers = network.getConnectedPeers();
        for (Peer peer : peers) {
            jsonArray.add(Utils.peer2Json(peer));
        }
        result.setCode(Result.ResultCode.SUCCESS.getCode());
        result.setMessage(Result.ResultCode.SUCCESS.getErrMsg());
        result.setData(jsonArray);

        return result;
    }

    @PatchMapping("/peer")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "连接节点", notes = "连接其他节点")
    public Result connectPeer(@RequestParam(value = "address") String address, @RequestParam(value = "port", defaultValue = "23333") int port) {
        Result result = new Result();

        try {
            Peer peer = new Peer(new PeerAddress(InetAddress.getByName(address), port), chain);
            network.addPeer(peer);
            result.setCode(Result.ResultCode.SUCCESS.getCode());
            result.setMessage(Result.ResultCode.SUCCESS.getErrMsg());
            result.setData(Utils.peer2Json(peer));
        } catch (UnknownHostException e) {
            logger.error("InetAddress has some problem. {}", e);
            result.setCode(Result.ResultCode.EXCEPTION.getCode());
            result.setMessage(Result.ResultCode.EXCEPTION.getErrMsg());
        }

        return result;
    }

    @GetMapping("/miner")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "获得矿工状态", notes = "获得矿工状态")
    public Result getMinerStatus() {
        Result result = new Result();
        JSONObject data = new JSONObject();

        if (miner.isWorking()) {
            data.put("status", "running");
        } else {
            data.put("status", "stop");
        }
        result.setCode(Result.ResultCode.SUCCESS.getCode());
        result.setMessage(Result.ResultCode.SUCCESS.getErrMsg());
        result.setData(data);

        return result;
    }

    @PatchMapping("/miner")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "启动矿工", notes = "启动矿工工作")
    public Result startMiner() {
        Result result = new Result();

        miner.run();
        JSONObject data = new JSONObject();
        result.setCode(Result.ResultCode.SUCCESS.getCode());
        result.setMessage(Result.ResultCode.SUCCESS.getErrMsg());
        if (miner.isWorking()) {
            data.put("status", "running");
        } else {
            data.put("status", "stop");
        }
        result.setData(data);

        return result;
    }

    @DeleteMapping("/miner")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "关闭矿工", notes = "停止矿工工作")
    public Result stopMiner() {
        Result result = new Result();

        miner.stop();
        JSONObject data = new JSONObject();
        result.setCode(Result.ResultCode.SUCCESS.getCode());
        result.setMessage(Result.ResultCode.SUCCESS.getErrMsg());
        if (miner.isWorking()) {
            data.put("status", "running");
        } else {
            data.put("status", "stop");
        }
        result.setData(data);

        return result;
    }

    @GetMapping("/network")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "获得网络状态", notes = "获得网络状态")
    public Result getNetworkStatus() {
        Result result = new Result();
        JSONObject data = new JSONObject();

        result.setCode(Result.ResultCode.SUCCESS.getCode());
        result.setMessage(Result.ResultCode.SUCCESS.getErrMsg());
        if (network.isRunning()) {
            data.put("status", "running");
        } else {
            data.put("status", "stop");
        }
        result.setData(data);


        return result;
    }

    @PatchMapping("/network")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "启动网络", notes = "启动网络")
    public Result startNetwork() {
        Result result = new Result();
        JSONObject data = new JSONObject();

        try {
            network.start();
            result.setCode(Result.ResultCode.SUCCESS.getCode());
            result.setMessage(Result.ResultCode.SUCCESS.getErrMsg());
            if (network.isRunning()) {
                data.put("status", "running");
            } else {
                data.put("status", "stop");
            }
            result.setData(data);
        } catch (IOException e) {
            logger.error("Peer network has some problem. {}", e);
            result.setCode(Result.ResultCode.EXCEPTION.getCode());
            result.setMessage(Result.ResultCode.EXCEPTION.getErrMsg());
        }

        return result;
    }

    @DeleteMapping("/network")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "停止网络", notes = "停止网络")
    public Result stopNetwork() {
        Result result = new Result();
        JSONObject data = new JSONObject();

        try {
            network.stop();
            result.setCode(Result.ResultCode.SUCCESS.getCode());
            result.setMessage(Result.ResultCode.SUCCESS.getErrMsg());
            if (network.isRunning()) {
                data.put("status", "running");
            } else {
                data.put("status", "stop");
            }
            result.setData(data);
        } catch (IOException e) {
            logger.error("Peer network has some problem. {}", e);
            result.setCode(Result.ResultCode.EXCEPTION.getCode());
            result.setMessage(Result.ResultCode.EXCEPTION.getErrMsg());
        }

        return result;
    }
}
