<template>
  <div>
    <h4 class="lighter">
      <i class="ace-icon fa fa-hand-o-right icon-animated-hand-pointer blue"></i>
      <router-link to="/business/course" class="pink"> {{course.name}} </router-link>
    </h4>
    <hr>
    <p>
      <router-link to="/business/course" class="btn btn-white btn-default btn-round">
        <i class="ace-icon fa fa-arrow-left"></i>
        返回课程
      </router-link>
      &nbsp;
      <button v-on:click="add()" class="btn btn-white btn-default btn-round">
        <i class="ace-icon fa fa-edit"></i>
        新增
      </button>
      &nbsp;
      <button v-on:click="list(1)" class="btn btn-white btn-default btn-round">
        <i class="ace-icon fa fa-refresh"></i>
        刷新
      </button>
    </p>

    <!--分页子组件-->
    <pagination ref="pagination" v-bind:list="list"></pagination>

    <table id="simple-table" class="table  table-bordered table-hover">
      <thead>
      <tr>
        <th>ID</th>
        <th>名称</th>
        <th>所属课程id</th>
        <th>操作</th>
      </tr>
      </thead>

      <tbody>
      <!--遍历集合-->
      <tr v-for="chapter in chapters">
        <td>{{chapter.id}}</td>
        <td>{{chapter.name}}</td>
        <td>{{chapter.courseId}}</td>
        <td>
          <div class="hidden-sm hidden-xs btn-group">
            <button v-on:click="toSection(chapter)" class="btn btn-white btn-xs btn-info btn-round">
              小节
            </button>&nbsp;
            <button v-on:click="edit(chapter)" class="btn btn-white btn-xs btn-info btn-round">
              编辑
            </button>&nbsp;
            <button v-on:click="del(chapter.id)" class="btn btn-white btn-xs btn-warning btn-round">
              删除
            </button>
          </div>
        </td>
      </tr>
      </tbody>
    </table>

    <div id="form-modal" class="modal fade" tabindex="-1" role="dialog">
      <div class="modal-dialog" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            <h4 class="modal-title">表单</h4>
          </div>
          <div class="modal-body">
            <form class="form-horizontal">
              <div class="form-group">
                <label class="col-sm-2 control-label">名称</label>
                <div class="col-sm-10">
                  <input v-model="chapter.name" class="form-control" placeholder="名称">
                </div>
              </div>
              <div class="form-group">
                <label class="col-sm-2 control-label">课程</label>
                <div class="col-sm-10">
                  <p class="form-control-static">{{course.name}}</p>
                </div>
              </div>
            </form>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
            <button v-on:click="save()" type="button" class="btn btn-primary">保存</button>
          </div>
        </div><!-- /.modal-content -->
      </div><!-- /.modal-dialog -->
    </div><!-- /.modal -->
  </div>
</template>

<script>
  import Pagination from "../../components/pagination"
  export default {
    components:{Pagination},
    name: "chapter",
    data: function() {
      return {
        chapter: {},
        chapters: [],
        course: {},
      }
    },
    mounted: function() {
      let _this = this

      _this.$refs.pagination.size = 5;
      //获取课程信息，根据课程信息查询大章
      let course = SessionStorage.get(SESSION_KEY_COURSE) || {};
      // if(Tool.isEmpty(course)){
      //   _this.$router.push("/welcome")
      // }
      console.log(course)
      _this.course = course
      //查询后台
      _this.list(1);

      // sidebar激活样式方法一
      this.$parent.activeSidebar("business-course-sidebar");

    },
    methods: {
      /**
       * 点击【新增】
       */
      add() {
        let _this = this;
        _this.chapter = {};
        $("#form-modal").modal("show");
      },

      /**
       * 点击【编辑】
       */
      edit(chapter) {
        let _this = this;
        //这里是把传进来的chapter对象复制一份出来
        _this.chapter = $.extend({}, chapter);
        $("#form-modal").modal("show");
      },

      /**
       * 列表查询
       */
      list(page) {
        let _this = this;
        Loading.show();
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/business/admin/chapter/list', {
        // _this.$ajax.post('http://127.0.0.1:9000/business/admin/chapter/list', {
          //不知为何，使用网关9000端口会引发跨域问题
          page: page,
          size: _this.$refs.pagination.size,
          courseId: _this.course.id
        }).then((response)=>{
          Loading.hide();
          let resp = response.data;
          console.log("查询大章列表结果：",resp)
          //后面还会改返回数据格式
          _this.chapters = resp.content.list;
          _this.$refs.pagination.render(page, resp.content.total);

        })
      },

      /**
       * 点击【保存】
       */
      save(page) {
        let _this = this;

        // 保存校验（名称不能为空，id长度需在1~8位）
        if (!Validator.require(_this.chapter.name, "名称")
          || !Validator.length(_this.chapter.courseId, "课程ID", 1, 8)) {
          return;
        }
        // 目前每个章节尚未有默认的课程id
        _this.chapter.courseId = _this.course.id;

        Loading.show();
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/business/admin/chapter/save', _this.chapter).then((response)=>{
        // _this.$ajax.post('http://127.0.0.1:9000/business/admin/chapter/save', _this.chapter).then((response)=>{
          Loading.hide();
          let resp = response.data;
          if (resp.success) {
            $("#form-modal").modal("hide");
            //重新查询
            _this.list(1);
            Toast.success("保存成功！");
          } else {
            Toast.warning(resp.message)
          }
        })
      },

      /**
       * 点击【删除】
       */
      del(id) {
        let _this = this;
        Confirm.show("删除大章后不可恢复，确认删除？", function () {
          Loading.show();
          _this.$ajax.delete(process.env.VUE_APP_SERVER + '/business/admin/chapter/delete/' + id).then((response)=>{
          // _this.$ajax.delete('http://127.0.0.1:9000/business/admin/chapter/delete/' + id).then((response)=>{
            Loading.hide();
            let resp = response.data;
            if (resp.success) {
              _this.list(1);
              //toast是我们自定义的弹框组件
              Toast.success("删除成功！");
            }
          })
        });
      },

      /**
       * 点击【小节】
       */
      toSection(chapter) {
        let _this = this;
        SessionStorage.set(SESSION_KEY_CHAPTER, chapter);
        _this.$router.push("/business/section");
      }
    }
  }
</script>