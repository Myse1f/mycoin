/**
 * Created By Yufan Wu
 * 2019/4/19
 */
package restapi;

import core.BlockChain;
import core.Miner;
import core.PeerGroup;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class RestfulController {
    private final BlockChain chain;

    private final PeerGroup network;

    private final Miner miner;

    @Autowired
    public RestfulController(BlockChain chain, PeerGroup network, Miner miner) {
        this.chain = chain;
        this.network = network;
        this.miner = miner;
    }

//    @GetMapping("/block")
//    @ResponseStatus(HttpStatus.OK)
//    @ApiOperation(value = "获取区块信息", notes = "根据hash获取区块信息")
//
//    @GetMapping("/recentblocks")
//    @ResponseStatus(HttpStatus.OK)
//    @ApiOperation(value = "获取最近的区块", notes = "获取最近的至多10个区块信息")
//
//    @GetMapping("/peers")
//    @ResponseStatus(HttpStatus.OK)
//    @ApiOperation(value = "获取临接节点", notes = "获取所有临接节点")
//
//    @PatchMapping("/peer")
//    @ResponseStatus(HttpStatus.CREATED)
//    @ApiOperation(value = "连接节点", notes = "连接其他节点")
//
//    @PatchMapping("/miner")
//    @ResponseStatus(HttpStatus.CREATED)
//    @ApiOperation(value = "启动矿工", notes = "启动矿工工作")
//
//    @DeleteMapping("/miner")
//    @ResponseStatus(HttpStatus.NO_CONTENT)
//    @ApiOperation(value = "关闭矿工", notes = "停止矿工工作")



}
